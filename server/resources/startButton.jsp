<%@ include file="/include.jsp"
%><jsp:useBean id="project" type="jetbrains.buildServer.serverSide.SProject" scope="request"
/><jsp:useBean id="bean" type="jetbrains.buildServer.serverSide.flaky.web.TestsAnalysisBean" scope="request"

/><c:set var="caption" value="${bean.testAnalysisEverStarted ? 'Start Again' : 'Start Now'}"/>

<div id="start-button">
  <span class="btn-group" style="font-size: 0;">
    <button class="btn btn_mini" onclick="return BS.Flaky.start('${project.projectId}');">${caption}</button>
    <button class="btn btn_mini" onclick="return BS.Flaky.Dialog.show();" title="Run custom build">...</button>
  </span>
  <forms:progressRing id="startAnalysisProgress" className="progressRingInline" style="display:none"/>
</div>
