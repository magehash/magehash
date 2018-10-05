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

  $(document).on("click", "[data-confirm]", function(e) {
    e.preventDefault();
    var el = e.target;
    var $this = $(this);

    swal({
      title: el.getAttribute("data-title") || "Are you sure?",
      text: el.getAttribute("data-text") || "If you click Yes, it will be deleted.",
      icon: el.getAttribute("data-icon") || "warning",
      buttons: {
        cancel: el.getAttribute("data-cancel") || "No",
        confirm: el.getAttribute("data-confirm-button") || "Yes",
      }
    })
    .then(function(confirm) {
      if (confirm) {
        $this.submit();
      }
    });
  })

  $("time").each(function() {
    var $this = $(this)
    var s = $this.text();
    var d = new Date(s);

    if($this.data("date")) {
      $this.text(d.toLocaleDateString())
    } else {
      $this.text(d.toLocaleDateString() + " " + d.toLocaleTimeString())
    }
  })
})
