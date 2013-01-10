<%@ include file="/include.jsp" %>
<jsp:useBean id="failureRate" type="jetbrains.buildServer.serverSide.flaky.data.FailureRate" scope="request"/>
<td class="rate" <bs:tooltipAttrs text="${failureRate.failures} failure${failureRate.failures != 1 ? 's' : ''} out of ${failureRate.totalRuns}"/>>
  <span class="fail">${failureRate.failures}</span>  / ${failureRate.totalRuns}
</td>
<td class="procent" <bs:tooltipAttrs text="Failure rate in this environment"/> >
  <fmt:formatNumber value="${(failureRate.failures / failureRate.totalRuns) * 100}"
                    minFractionDigits="1"
                    maxFractionDigits="1" />%
</td>
