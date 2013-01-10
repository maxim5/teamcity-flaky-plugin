<%@ include file="/include.jsp" %><%@
    taglib prefix="bs" tagdir="/WEB-INF/tags"
%><jsp:useBean id="testDetails" type="jetbrains.buildServer.serverSide.flaky.web.TestWebDetails" scope="request"

/><div class="test-details">
  <c:if test="${testDetails.hasReason}">
    <div class="reason">
      <c:choose>
        <c:when test="${testDetails.withoutChangesReason}">
          <div>Diagnosis: test failed in a build without changes: <bs:buildLinkFull build="${testDetails.buildWithoutChanges}"/></div>
        </c:when>
        <c:when test="${testDetails.buildsOnSameModificationReason}">
          <div>Diagnosis: test run differently in builds with same sources:</div>

          <table class="modificationBuilds">
            <tr class="buildTypeProblem">
              <td class="fail">Failed in:</td>
              <bs:changeRequest key="build" value="${testDetails.failedInBuild}">
                <jsp:include page="buildTd.jsp"/>
              </bs:changeRequest>
            </tr>

            <tr class="buildTypeProblem">
              <td class="success">Successful in:</td>
              <bs:changeRequest key="build" value="${testDetails.successfulInBuild}">
                <jsp:include page="buildTd.jsp"/>
              </bs:changeRequest>
            </tr>
          </table>
        </c:when>
        <c:when test="${testDetails.suspiciousStatisticsReason}">
          <c:set var="suspiciousStats" value="${testDetails.suspiciousStatistics}"/>
          <div>Diagnosis: test had <b>${suspiciousStats.second}</b> series of consecutive failures with average length of
               <b><fmt:formatNumber value="${suspiciousStats.first / suspiciousStats.second}"
                                    minFractionDigits="1"
                                    maxFractionDigits="1" /></b> builds</div>
        </c:when>
      </c:choose>
    </div>
  </c:if>

  <div class="stats">
    <c:set var="stats" value="${testDetails.stats}"/>

    Total test runs: <b>${stats.first}</b>, total failures: <b>${stats.second}</b>,
    failure rate: <b><fmt:formatNumber value="${stats.first > 0 ? (stats.second / stats.first) * 100 : 0}"
                                       minFractionDigits="1"
                                       maxFractionDigits="1" />%</b>
    <%--<br>
    Failure rates in different build configurations and agents:--%>
  </div>

  <table>
    <tr>
      <td class="block">
        <div class="block">
          <div class="title">Failure statistics by all build configurations</div>
          <div class="content">
            <table>
              <c:forEach items="${testDetails.allBuildTypes}" var="bt">
                <c:set var="failureRate" value="${testDetails.testData.buildTypeFailureRates[bt.buildTypeId]}"/>
                <tr ${failureRate.failures == 0 ? "class='zero'" : ""}>
                  <td><bs:buildTypeLink buildType="${bt}"/></td>
                  <bs:changeRequest key="failureRate" value="${failureRate}">
                    <jsp:include page="failureRate.jsp"/>
                  </bs:changeRequest>
                </tr>
              </c:forEach>
            </table>
          </div>
        </div>
      </td>
      <td class="block">
        <div class="block">
          <div class="title">Failure statistics by all agents</div>
          <div class="content">
            <table>
              <c:forEach items="${testDetails.allAgents}" var="agent">
                <c:set var="failureRate" value="${testDetails.testData.agentFailureRates[agent.name]}"/>
                <tr ${failureRate.failures == 0 ? "class='zero'" : ""}>
                  <%--<td><bs:agentDetailsLink agent="${agent}"/></td>--%>
                  <td><bs:agentDetailsFullLink agent="${agent}"
                                               doNotShowOutdated="true"
                                               doNotShowOSIcon="false"
                                               doNotShowPoolInfo="true"
                                               showRunningStatus="false"
                                               doNotShowUnavailableStatus="true"/></td>
                  <bs:changeRequest key="failureRate" value="${failureRate}">
                    <jsp:include page="failureRate.jsp"/>
                  </bs:changeRequest>
                </tr>
              </c:forEach>
            </table>
          </div>
        </div>
      </td>
      <td></td>
    </tr>
  </table>

  <c:url var="testDetailUrl"
         value="/project.html?tab=testDetails&testNameId=${testDetails.testData.testId}&projectId=${testDetails.testData.projectId}"/>
  <a href="${testDetailUrl}" title="View test details">Complete test details &raquo;</a>
</div>
