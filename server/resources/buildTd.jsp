<%@ include file="/include.jsp"
%><c:if test="${not empty build}">
  <td class="bt"><bs:buildTypeLink buildType="${build.buildType}"/></td>
  <td class="build"><%@ include file="/changeBuild.jspf" %></td>
</c:if
><c:if test="${empty build}">
  <td colspan="2"><i>build no longer exists (please restart the analyzer)</i></td>
</c:if>
