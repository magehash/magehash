function assets(count) {
  var $site = $("#site");

  if($site.length > 0) {
    $.get($site.data("url"), function(html) {

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
        }, 1000);
      }
    })
  }
}

$(function() {
  assets(1);
})
