<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<c:set var="contextPath" value="${pageContext.request.contextPath}"/>
<html>
	<head>
		<link href="${contextPath}/webresources/css/frontdoor.css" rel="stylesheet" type="text/css">
		<link href="${contextPath}/webresources/favicon.ico" rel="shortcut icon" >
	</head>
	<body>
		<c:set var="contextPath" value="${pageContext.request.contextPath}"/>
	<div class="body">
		<jsp:useBean id="createdDate" class="java.util.Date" />
		<ul class="imageList">
			<c:forEach var="i" items="${imageKeys}">
				<li class="historyImage">
					<a href="cachedImage?index=${i}" target="_blank" alt="Show large size">
						<img border="0" id="frontdoorimage${i}" name="voordeur" alt="Voordeur" src="cachedThumbnail?index=${i}"/>
					</a><jsp:setProperty name="createdDate" property="time" value="${i}" />
					<fmt:formatDate type="time" value="${createdDate}" />
				</li>
			</c:forEach>	
			</ul>
	</div>
	<div class="bottom">
		<p>Pagina <a onClick="window.location.reload()" href="#">herladen</a></p>
		<p>Naar <a href="${contextPath}/">home</a></p>
		<div>
			${numberOfImages} plaatjes in geheugen.<br/>
			${memoryInfo}
		</div>
	</div>
	
	</body>
</html>
