<%@ include file="/include.jsp" %><%@
    taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %><%@
    taglib prefix="tt" tagdir="/WEB-INF/tags/tests" %><%@
    taglib prefix="bs" tagdir="/WEB-INF/tags"
%><jsp:useBean id="bean" type="jetbrains.buildServer.serverSide.flaky.web.TestsAnalysisBean" scope="request"

/><bs:refreshable containerId="flaky" pageUrl="${pageUrl}">
  <c:choose>
    <c:when test="${bean.inProgress}">
      <forms:progressRing className="progressRingInline"/> ${bean.progress.currentStep}
      <c:if test="${bean.progress.totalSize > 0}">
        <b><fmt:formatNumber value="${(bean.progress.doneSize / bean.progress.totalSize) * 100}"
                             maxFractionDigits="0" />%</b> done
        (${bean.progress.doneSize} of ${bean.progress.totalSize})
      </c:if>
      <script type="text/javascript">
        BS.Flaky.scheduleReload();
      </script>
    </c:when>

    <c:when test="${not bean.hasData}">
      <c:choose>
        <c:when test="${bean.testAnalysisEverStarted}">
          <jsp:include page="printResultShort.jsp"/>
        </c:when>
        <c:otherwise>
          No tests were analyzed in this project so far.
          <jsp:include page="startButton.jsp"/>
        </c:otherwise>
      </c:choose>
    </c:when>

    <c:when test="${bean.hasData}">
      <jsp:include page="printResultShort.jsp"/>

      <c:if test="${bean.hasFlaky}">
        <c:set var="title">Environment-dependent tests: ${bean.flakyTestsSize}</c:set>
        <bs:_collapsibleBlock title="${title}" id="flakyTestsBlock">
          <bs:trimWhitespace
            ><tt:testGroupWithActions groupedTestsBean="${bean.flakyTests}"
                                      defaultOption="package"
                                      groupSelector="true"
                                      id="flaky">
              <jsp:attribute name="afterToolbar">
                <td class="env">Environment specifics</td>
                <td>&nbsp;</td>
              </jsp:attribute>
              <jsp:attribute name="testAfterName">
                <bs:changeRequest key="testBean" value="${testBean}">
                  <jsp:include page="environmentDetails.jsp"/>
                </bs:changeRequest>
              </jsp:attribute>
             </tt:testGroupWithActions
          ></bs:trimWhitespace>
        </bs:_collapsibleBlock>
      </c:if>

      <c:if test="${bean.hasSuspicious}">
        <c:set var="title">Suspicious tests: ${bean.suspiciousTestsSize}</c:set>
        <bs:_collapsibleBlock title="${title}" id="suspiciousTestsBlock">
          <tt:testGroupWithActions groupedTestsBean="${bean.suspiciousTests}"
                                   defaultOption="package"
                                   groupSelector="true"
                                   id="suspicious">
            <jsp:attribute name="afterToolbar">
              <td class="env">Environment specifics</td>
              <td>&nbsp;</td>
            </jsp:attribute>
            <jsp:attribute name="testAfterName">
              <bs:changeRequest key="testBean" value="${testBean}">
                <jsp:include page="environmentDetails.jsp"/>
              </bs:changeRequest>
            </jsp:attribute>
          </tt:testGroupWithActions>
        </bs:_collapsibleBlock>
      </c:if>

      <c:if test="${bean.hasAlwaysFailing}">
        <c:set var="title">Tests that always fail: ${bean.alwaysFailingTestsSize}</c:set>
        <bs:_collapsibleBlock title="${title}" id="alwaysFailingTestsBlock" collapsedByDefault="true">
          <tt:testGroupWithActions groupedTestsBean="${bean.alwaysFailingTests}"
                                         defaultOption="package"
                                         groupSelector="true"
                                         id="always">
          </tt:testGroupWithActions>
        </bs:_collapsibleBlock>
      </c:if>

      <script type="text/javascript">
        BS.Flaky.scrollAndExpandIfNeeded();
      </script>
    </c:when>
  </c:choose>

  <jsp:include page="customSettingsDialog.jsp"/>
</bs:refreshable>
