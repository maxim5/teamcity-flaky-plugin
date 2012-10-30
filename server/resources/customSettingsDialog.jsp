<%@ include file="/include.jsp"
%><jsp:useBean id="bean" type="jetbrains.buildServer.serverSide.flaky.web.TestsAnalysisBean" scope="request"/>

<bs:modalDialog formId="settingsForm"
                title="Custom Test Analysis"
                action=""
                closeCommand="return BS.Flaky.Dialog.cancel();"
                saveCommand="return BS.Flaky.Dialog.start('${bean.project.projectId}');">
  <c:set var="settings" value="${bean.testAnalysisResult.settings}"/>

  <div id="dialog-tabs" class="simpleTabs clearfix" style="margin-bottom: 0.5em;">
    <ul class="tabs">
      <li class="first selected" id="tab-0"><p><a href="#">Build configurations filter</a></p></li>
      <li id="tab-1"><p><a href="#">Time period</a></p></li>
      <li class="last" id="tab-2"><p><a href="#">Heuristics</a></p></li>
    </ul>
  </div>

  <div id="dialog-contents">
    <div id="tab-0-content">
      <select id="excludeBuildTypes" multiple="multiple">
        <c:forEach items="${bean.buildTypeSettings}" var="entry">
          <forms:option value="${entry.key.buildTypeId}" selected="${entry.value}">
            <c:out value="${entry.key.name}"/>
          </forms:option>
        </c:forEach>
      </select>

      <div class="grayNote">Selected build configurations will not be used in analysis</div>
    </div>

    <div id="tab-1-content" style="display: none;">
      <div style="margin-bottom: 1em;">
        <forms:checkbox name="analyseFullHistory" checked="${settings.analyseFullHistory}"/>
        <label for="analyseFullHistory">Analyse full history for each test</label>

        <div class="grayNote">Unchecking this option may speed-up the analysis</div>
      </div>

      <div>
        <table>
          <tr>
            <td class="head">
              <label for="analyseTimePeriodDays">Time period (days):</label>
            </td>
            <td>
              <forms:textField name="analyseTimePeriodDays" value="${settings.analyseTimePeriodDays}"
                               disabled="${settings.analyseFullHistory}"/>
            </td>
          </tr>
        </table>
        <div class="grayNote">Only builds started in a specified time slot will be processed</div>
      </div>
    </div>

    <div id="tab-2-content" style="display: none;">
      <div style="margin-bottom: 1em;">
        <forms:checkbox name="speedUpAlwaysFailing" checked="${settings.speedUpAlwaysFailing}"/>
        <label for="speedUpAlwaysFailing">Speed up processing of tests with 100% failure rate</label>

        <div class="grayNote">Checking this option may produce inaccurate results, but work significantly faster</div>
      </div>

      <div>
        <table>
          <tr>
            <td class="head">
              <label for="minSeriesNumber">Minimum series number:</label>
            </td>
            <td>
              <forms:textField name="minSeriesNumber" value="${settings.minSeriesNumber}"/>
            </td>
          </tr>
          <tr>
            <td class="head">
              <label for="averageSeriesLength">Maximum average series length:</label>
            </td>
            <td>
              <forms:textField name="averageSeriesLength" value="${settings.averageSeriesLength}"/>
            </td>
          </tr>
        </table>
      </div>
      <div class="grayNote">
        The values are used to determine tests that fail suspiciously too often.
        The <i>series</i> means a collection of consecutive test failures.
      </div>
    </div>
  </div>

  <div class="popupSaveButtonsBlock">
    <forms:cancel onclick="return BS.Flaky.Dialog.cancel();"/>
    <forms:submit label="Start" onclick="return BS.Flaky.Dialog.start('${bean.project.projectId}');"/>
  </div>
</bs:modalDialog>
