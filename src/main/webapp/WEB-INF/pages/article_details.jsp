<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<!doctype html>
<html>
<head>
<meta charset="utf-8">
<title>文章详情</title>
<link rel="stylesheet" href="${pageContext.request.contextPath}/static/css/bootstrap.min.css"/>
</head>

<body>
<div class="container-fluid">
  <div class="row">
  	<div class="col-sm-12">
        <h1>${article.title }</h1>
        <p>
        	${article.details }
        </p>
    </div>
  </div>
</div>


<script src="${pageContext.request.contextPath}/static/js/jquery.min.js"></script>
<script src="${pageContext.request.contextPath}/static/js/bootstrap.min.js"></script>
</body>
</html>
