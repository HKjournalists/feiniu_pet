<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<%@ taglib prefix="s" uri="/struts-tags"%>
<%@ taglib prefix="mis" uri="/tld/lvmama-tags.tld"%>
<s:set var="basePath"><%=request.getContextPath()+"/"%></s:set>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>导入excel数据</title>
<link rel="stylesheet" type="text/css" href="${basePath}css/ui-common.css"></link>
<link rel="stylesheet" type="text/css" href="${basePath}css/ui-components.css"></link>
<link rel="stylesheet" type="text/css" href="${basePath}css/panel-content.css"></link>
</head>
<body>
<s:include value="/WEB-INF/pages/back/base/jquery.jsp"/>
<script src="${basePath}/js/base/jquery.form.js"></script>
<script type="text/javascript">
function checkForm(){
    if($.trim($("#upload_file").val())==''){
        alert("上传文件不可以为空");
        return false;
    }
    
    var filePath =$("#upload_file").val();
    if (filePath.lastIndexOf(".") == -1) {
        alert("文件类型错误");
        return;
    }else {
         var suffix=filePath.substring(filePath.lastIndexOf("."));
         if(!(suffix=='.xls'||suffix=='.xlsx')){
             alert("文件名后缀不对请重新上传!");
         }
    }
    
 		var action="${basePath}/distribution/batch/upCodeAndCancelOrder.do";
 		var options = { 
                url:action,
                dataType:"",
                async:false,
                type : "POST", 
                success:function(dt){ 
                	var data=eval("(" + dt + ")");
                	alert(data.msg);
                }, 
                error:function(){ 
                    alert("操作超时！"); 
                } 
            };
            $('#excelFrom').ajaxSubmit(options);
    return true;
}

	function getFileName(obj) {
		var filePath = obj.value;
 		if (filePath.lastIndexOf(".") == -1) {
			alert("文件类型错误");
			return;
		}else {
			 var suffix=filePath.substring(filePath.lastIndexOf("."));
			 if(!(suffix=='.xls'||suffix=='.xlsx')){
 				 alert("文件名后缀不对请重新上传!");
			 }
		}

	}
</script>
<form method="post" id="excelFrom" enctype="multipart/form-data"  >
        <h3>导入excel数据</h3>
        <div>选择文件:<s:file id="upload_file" name="file" label="文件" cssStyle="width:300px;" onchange="javascript:getFileName(this);"/><br /><br />
        <input type="button" class="btn btn-small w5" value="导入" onclick="javascript:return checkForm();"/></div>
</form>
</body>
</html>