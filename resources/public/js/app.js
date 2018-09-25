function assets(count) {
  var $site = $("#site");

  console.log("assets");

  if($site.length > 0) {
    $.get("/site/" + $site.data("id") + "/assets?job-id=" + $("#job").data("id"), function(html) {
      console.log(html)
      if(!!html) {
        $("#site").remove();
        $("#assets").html(html);
      } else {
        var dots = ""
        for(i = 0; i < count; i++) {
          dots += "."
        }

        $("#assets").text("Hasing in progress..." + dots)

        setTimeout(function() {
          assets(count + 1);
        }, 3000);
      }
    })
  }
}

$(function() {
  assets(1);
})
