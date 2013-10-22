<%@ include file="/include.jsp"%>
<%--@elvariable id="testData" type="jetbrains.buildServer.serverSide.flaky.data.TestData"--%>
<c:if test="${not empty testData}">
  <style type="text/css">
    .warn {
      background: rgb(255, 255, 234);
      padding: 0.5em 1em;
      border: 1px solid rgb(228, 228, 228);
      margin-top: 1em;
      margin-bottom: 0.5em;
      display: inline-block;
    }

    img.dice {
      vertical-align: top;
      margin-right: 0.3em;
    }
  </style>

  <%--@elvariable id="project" type="jetbrains.buildServer.serverSide.SProject"--%>
  <%--@elvariable id="isFlaky" type="java.lang.Boolean"--%>
  <%--@elvariable id="isSuspicious" type="java.lang.Boolean"--%>
  <%--@elvariable id="isAlwaysFailing" type="java.lang.Boolean"--%>
  <%--@elvariable id="iconUrl" type="java.lang.String"--%>
  <c:url var="url" value="/project.html?projectId=${project.externalId}&tab=analysis#test${testData.testId}"/>
  <c:url var="iconUrl" value="${iconUrl}"/>
  <c:choose>
    <c:when test="${isFlaky}">
      <div class="warn">
        <img class="dice" src="${iconUrl}"/>
        This test looks like <b>flaky</b>, because its results across all configurations are environment-dependent.
        <a href="${url}">More details &raquo;</a>
      </div>
    </c:when>
    <c:when test="${isSuspicious}">
      <div class="warn">
        <img class="dice" src="${iconUrl}"/>
        This test looks like <b>flaky</b> based on failure statistics in all configurations.
        <a href="${url}">More details &raquo;</a>
      </div>
    </c:when>
    <c:when test="${isAlwaysFailing}">
      <div class="warn">
        This test does not have successful runs in any build configuration.
        <a href="${url}">More details &raquo;</a>
      </div>
    </c:when>
  </c:choose>
</c:if>