<%@ include file="/include.jsp" %>
<jsp:useBean id="failureRate" type="jetbrains.buildServer.serverSide.flaky.data.FailureRate" scope="request"/>
<td class="procent" <bs:tooltipAttrs text="${failureRate.failures} / ${failureRate.totalRuns}"/> >
  <fmt:formatNumber value="${(failureRate.failures / failureRate.totalRuns) * 100}"
                    minFractionDigits="1"
                    maxFractionDigits="1" />%
</td>
