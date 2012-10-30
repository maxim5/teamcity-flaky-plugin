<%@ include file="/include.jsp"
%><jsp:useBean id="bean" type="jetbrains.buildServer.serverSide.flaky.web.TestsAnalysisBean" scope="request"
/><c:set var="testAnalysisResult" value="${bean.testAnalysisResult}" />
<div>
  Last test analysis performed <bs:elapsedTime time="${testAnalysisResult.finishDate}"/> (completed in ${bean.testAnalysisDuration}).
  Total tests analysed: <b>${testAnalysisResult.totalTests}</b>.
</div>
<div>
  <c:set var="settings" value="${testAnalysisResult.settings}"/>
  Settings used:
  <c:if test="${empty settings.excludeBuildTypes}">all build configurations included,</c:if>
  <c:if test="${not empty settings.excludeBuildTypes}">
    <bs:changeRequest key="buildTypes" value="${bean.excludedBuildTypes}">
      <jsp:include page="buildTypes.jsp"/>
    </bs:changeRequest>
    excluded,
  </c:if>

  <c:if test="${settings.analyseFullHistory}">full tests history processed,</c:if>
  <c:if test="${not settings.analyseFullHistory}">tests history for last ${settings.analyseTimePeriodDays} days processed,</c:if>

  magic numbers (heuristics): <c:if test="${settings.speedUpAlwaysFailing}">fast processing of tests with 100% failure rate,</c:if>
  minimum series number <b>${settings.minSeriesNumber}</b>, maximum average series length <b>${settings.averageSeriesLength}</b>.
</div>

<jsp:include page="startButton.jsp"/>
