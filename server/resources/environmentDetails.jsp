<%@ include file="/include.jsp"
%><jsp:useBean id="testBean" type="jetbrains.buildServer.web.problems.STestBean" scope="request"
/><jsp:useBean id="bean" type="jetbrains.buildServer.serverSide.flaky.web.TestsAnalysisBean" scope="request"

/><c:set var="test" value="${testBean.run.test}"
/><c:set var="details" value="${bean.details[test.testNameId]}"/>
<%--@elvariable id="details" type="jetbrains.buildServer.serverSide.flaky.web.TestWebDetails"--%>

</td>
<td class="env">
  <c:if test="${details.failedOnlyInSingleBuildType or details.failedOnlyOnSingleAgent}">
    fails only
    <c:if test="${details.failedOnlyInSingleBuildType}">
      in <bs:buildTypeLink buildType="${details.failedInBuildTypes[0]}"/>
    </c:if>
    <c:if test="${details.failedOnlyOnSingleAgent}">
      on <bs:agentDetailsLink agent="${details.failedOnAgents[0]}"/>
    </c:if>
  </c:if>
</td>
<td>
