/*
 * Copyright (c) 2000-2012 by JetBrains s.r.o. All Rights Reserved.
 * Use is subject to license terms.
 */
package jetbrains.buildServer.serverSide.flaky;

import jetbrains.ServerTestUtil;
import jetbrains.buildServer.BaseTestCase;
import jetbrains.buildServer.ExtensionAccessor;
import jetbrains.buildServer.TeamCityExtension;
import jetbrains.buildServer.serverSide.BuildServerListener;
import jetbrains.buildServer.serverSide.ProjectIdProvider;
import jetbrains.buildServer.serverSide.ServerPaths;
import jetbrains.buildServer.serverSide.ServerSideEventDispatcher;
import jetbrains.buildServer.serverSide.agentPools.AgentPoolManager;
import jetbrains.buildServer.serverSide.agentPools.AgentPoolManagerImpl;
import jetbrains.buildServer.serverSide.agentTypes.AgentTypeDatabaseStorage;
import jetbrains.buildServer.serverSide.artifacts.ArtifactsGuardImpl;
import jetbrains.buildServer.serverSide.db.*;
import jetbrains.buildServer.serverSide.db.jdbcLoader.JdbcDriverLoader;
import jetbrains.buildServer.serverSide.db.jdbcLoader.JdbcDrivers;
import jetbrains.buildServer.serverSide.impl.*;
import jetbrains.buildServer.serverSide.impl.auth.RolesManagerImpl;
import jetbrains.buildServer.serverSide.impl.auth.SecurityContextImpl;
import jetbrains.buildServer.serverSide.settings.ProjectSettingsManagerImpl;
import jetbrains.buildServer.serverSide.versioning.VersionManager;
import jetbrains.buildServer.util.EventDispatcher;
import jetbrains.buildServer.util.PropertiesUtil;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.testng.annotations.BeforeMethod;

import java.io.File;
import java.io.IOException;
import java.sql.Driver;
import java.sql.DriverManager;
import java.util.Collections;
import java.util.Date;
import java.util.Map;

/**
 * @author Maxim Podkolzine (maxim.podkolzine@jetbrains.com)
 * @since 8.0
 */
public class BaseServerTestCase extends BaseTestCase {
  protected ServerPaths myServerPaths;
  protected TeamCityDatabaseManager myDbManager;
  protected DBFacade myDbFacade;

  protected SecurityContextImpl mySecurityContext;
  protected EventDispatcher<BuildServerListener> myEventDispatcher;
  protected BuildServerImpl myServer;
  protected ProjectManagerImpl myProjectManager;
  protected TestManagerImpl myTestManager;
  protected AgentPoolManager myAgentPoolManager;

  @BeforeMethod
  @Override
  protected void setUp() throws Exception {
    super.setUp();

    myServerPaths = new ServerPaths(ServerTestUtil.getSystemDir(),
                                    ServerTestUtil.getConfigDir(),
                                    ServerTestUtil.getBackupDir());
    myDbManager = createTeamCityDatabaseManager(getDatabaseSettings());
    myDbFacade = new DBFacade(myDbManager);
    createSchemaFromScratch();

    mySecurityContext = new SecurityContextImpl();
    myEventDispatcher = ServerSideEventDispatcher.create(mySecurityContext,
                                                         BuildServerListener.class);
    myServer = new BuildServerImpl(myServerPaths, mySecurityContext, myEventDispatcher);
    myServer.setAccessor(new ExtensionAccessor() {
      @NotNull
      public <T> Map<String, T> getRegisteredExtensions(@NotNull Class<T> tClass) {
        return Collections.emptyMap();
      }
      public <T extends TeamCityExtension> void registerExtension(@NotNull Class<T> tClass, @NonNls @NotNull String s, @NotNull T t) {
      }
      public <T extends TeamCityExtension> void unregisterExtension(@NotNull Class<T> tClass, @NonNls @NotNull String s) {
      }
    });

    myProjectManager = new ProjectManagerImpl(myEventDispatcher,
                                              myServerPaths,
                                              new ProjectDataModelImpl(),
                                              mySecurityContext,
                                              new ProjectSettingsManagerImpl(myEventDispatcher),
                                              new ArtifactsGuardImpl(myServerPaths));
    myProjectManager.setServer(myServer);
    myProjectManager.setFileWatcherFactory(new FileWatcherFactory(myServerPaths));
    ConfigurationErrorsImpl configurationErrors = new ConfigurationErrorsImpl(new CriticalErrorsImpl());
    myProjectManager.setConfigErrors(configurationErrors);
    myProjectManager.setCopyProjectHelper(new CopyProjectHelper());
    myProjectManager.setProjectIdProvider(new StaticIdProvider());
    myProjectManager.setRolesManager(new RolesManagerImpl(myServerPaths, mySecurityContext));

    myAgentPoolManager = new AgentPoolManagerImpl(myDbFacade,
                                                  new AgentTypeDatabaseStorage(myDbFacade, myDbFacade),
                                                  mySecurityContext);
    myProjectManager.setAgentPoolManager(myAgentPoolManager);
    myTestManager = new TestManagerImpl();
  }

  @NotNull
  private static TeamCityDatabaseManager createTeamCityDatabaseManager(@NotNull DatabaseSettings dbSettings) {
    TeamCityDatabaseManager dbManager = new TeamCityDatabaseManager();
    File jdbcDriversDir = getJdbcDriversDir();

    String runPerSessionScriptPropertyValue = System.getProperty("teamcity.database.runPerSessionScript", "true");
    boolean runPerSessionScript = PropertiesUtil.getBoolean(runPerSessionScriptPropertyValue);

    String[] perSessionScript = runPerSessionScript ? DBSchema.generatePerSessionScript(dbSettings.getDatabaseType()) : null;

    JdbcDriverLoader jdbcLoader = new JdbcDriverLoader();
    JdbcDrivers jdbcDrivers = jdbcLoader.loadJdbcDrivers(jdbcDriversDir);
    dbManager.setup(jdbcDrivers, dbSettings, perSessionScript);

    dbManager.connect(true);
    return dbManager;
  }

  private static File getJdbcDriversDir() {
    System.out.println(new File(".").getAbsolutePath());
    return new File("./lib/jdbc");
  }

  private static final String DRIVER_NAME = "org.hsqldb.jdbcDriver";
  private static final String JDBC_HSQLDB_MEMORY_URL = "jdbc:hsqldb:mem:buildserver";

  static {
    try {
      DriverManager.registerDriver((Driver) Class.forName(DRIVER_NAME).newInstance());
    }
    catch (Throwable t) {
      throw new RuntimeException(t);
    }
  }

  private static DatabaseSettings getDatabaseSettings() {
    DatabaseSettings settings = new DatabaseSettings(JDBC_HSQLDB_MEMORY_URL, " ");
    settings.setMaxConnections(20);
    return settings;
  }

  /**
   * Creates DB schema with data.
   * Drops all tables before creating (if any exist).
   */
  public void createSchemaFromScratch() {
    // dropAllTables();

    withDB(new DBActionNoResults() {
      public void run(final DBFunctions dbf) throws DBException {

        DBSchema.createSchema(dbf, true);

      }
    }, true);

    try {
      VersionManager.writeVersionToDisk(myServerPaths, DBSchema.getCurrentVersion(), new Date());
    } catch (IOException ioe) {
      throw new RuntimeException("Could not write version file to disk: " + ioe.getMessage(), ioe);
    }

    VersionManager.ourDatabaseCreatedOnStartup = true;
  }

  public void withDB(final @NotNull DBActionNoResults action, boolean commitOnSuccess) {
    withDB(new DBAction<Void>() {
      public Void run(final DBFunctions dbf) throws DBException {
        action.run(dbf);
        return null;
      }
    }, commitOnSuccess);
  }

  public <T> T withDB(@NotNull DBAction<T> action, boolean commitOnSuccess) {
    DBFunctions dbf;
    dbf = DBFunctions.create(myDbManager.getDatabaseType(),
                             myDbManager.getDataSource());

    T result;
    boolean committed = false;

    try {
      result = action.run(dbf);
      if (commitOnSuccess) {
        dbf.commit();
        committed = true;
      }
    } finally {
      try {
        if (!committed)
          dbf.rollback();
      } finally {
        dbf.close();
      }
    }

    return result;
  }

  static class StaticIdProvider implements ProjectIdProvider {
    private int counter = 1;
    private int proj_counter = 1;

    public synchronized String getNextProjectId() {
      return "project" + (proj_counter++);
    }

    public synchronized String getNextBuildTypeId() {
      return "bt" + (counter++);
    }

    public synchronized String getNextBuildTypeTemplateId() {
      return "btTemplate" + (counter++);
    }

    void resetCounter() {
      counter = 1;
    }
  }
}
