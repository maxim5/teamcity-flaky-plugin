<%@ include file="/include.jsp" %><%@
    taglib prefix="util" uri="/WEB-INF/functions/util"
  %><jsp:useBean id="buildTypes" type="java.util.List" scope="request"

  /><c:set var="id" value="${util:uniqueId()}"
  /><c:choose>
<c:when test="${fn:length(buildTypes) == 1}"><bs:buildTypeLink buildType="${buildTypes[0]}"/> build configuration</c:when>
<c:otherwise>
  <bs:simplePopup controlId="popup${id}" linkOpensPopup="true"
                  popup_options="shift: {x: -140, y: 20}">
    <jsp:attribute name="content">
      <c:forEach items="${buildTypes}" var="bt">
        <div>
          <bs:buildTypeLinkFull buildType="${bt}"/>
        </div>
      </c:forEach>
    </jsp:attribute>
    <jsp:body>
      ${fn:length(buildTypes)} build configurations
    </jsp:body>
  </bs:simplePopup>
</c:otherwise>
</c:choose>
