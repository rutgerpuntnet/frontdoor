<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<c:set var="contextPath" value="${pageContext.request.contextPath}" />
<html>
<head>
<script
	src="//ajax.googleapis.com/ajax/libs/jquery/1.11.1/jquery.min.js"></script>
<script
	src="${contextPath}/webresources/owl-carousel/owl.carousel.min.js"></script>
<link href="${contextPath}/webresources/img/favicon.ico"
	rel="shortcut icon">
<link href="${contextPath}/webresources/css/frontdoor.css"
	rel="stylesheet" type="text/css">
<link rel="stylesheet"
	href="${contextPath}/webresources/owl-carousel/owl.carousel.css">
<link rel="stylesheet"
	href="${contextPath}/webresources/owl-carousel/owl.theme.css">

<link rel="apple-touch-icon-precomposed"
	href="${contextPath}/webresources/img/cctv-dome-camera-120-178024.png" />
<link rel="apple-touch-icon"
	href="${contextPath}/webresources/img/cctv-dome-camera-64-178024.png">
<link rel="apple-touch-icon" sizes="72x72"
	href="${contextPath}/webresources/img/cctv-dome-camera-72-178024.png">
<link rel="apple-touch-icon" sizes="120x120"
	href="${contextPath}/webresources/img/cctv-dome-camera-120-178024.png">
<link rel="apple-touch-icon" sizes="152x152"
	href="${contextPath}/webresources/img/cctv-dome-camera-152-178024.png">
<script>
	$(document).ready(function() {
		var owl = $("#carousel");
		owl.owlCarousel({
			items : 6,
			itemsDesktop : [1040,4],
			lazyLoad : true,
			navigation : true,
			navigationText : ["Vorige","Volgende"]
		});
	});
    $(function(){
        //animate loading text
        $("#spanLoading").animate({left: '+=50'},500); 

        //On img loaded, remove loading text
        $("#frontdoorimage").load(){
            $("#spanLoading").remove();
        }
    });
</script>
</head>
<body
	style="margin: 0; padding: 0; background-color: black; color: graytext;">
	<div class="headInfo">
		<c:if test="${oldImage}">
			<div class="message">
				<h1>Deze laatst beschikbare foto is ouder dan 30 seconden. De
					camera is momenteel niet bereikbaar.</h1>
			</div>
		</c:if>
	</div>
	<div class="frontdoorImage">
		<div class="mainImage">
			<img id="frontdoorimage" name="voordeur" alt="Voordeur"
				src="/voordeur/latestImage" onClick="window.location.reload()" />
			<div class="desc">Afbeelding van: ${imageDate}</div>
			<span id="spanLoading">Afbeelding wordt geladen...</span>
		</div>

		<br />
		<br />
		<!-- todo add spacing with css -->
			<jsp:useBean id="createdDate" class="java.util.Date" />
		<div id="carousel" class="owl-carousel">
			<c:forEach var="i" items="${imageKeys}">
				<div class="mainImage">
					<div class="item"
						onclick="window.open('cachedImage?index=${i}','_blank')">
						<img class="lazyOwl" data-src="cachedThumbnail?index=${i}"
							alt="Frontdoor history image">
					</div>
					<div class="descSmall">
						<jsp:setProperty name="createdDate" property="time" value="${i}" />
						<fmt:formatDate type="time" value="${createdDate}" />
					</div>
				</div>
			</c:forEach>
		</div>
 	</div>
</body>
</html>
