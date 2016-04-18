function (d, w, c) {
  (w[c] = w[c] || []).push(function() {
    try {
      if (typeof(_yaparams) != 'undefined') {
        w.yaCounter24049213 = new Ya.Metrika({
          id: 24049213,
          webvisor: true,
          clickmap: true,
          trackLinks: true,
          accurateTrackBounce: true,
          params: _yaparams
        });
      } else {
        w.yaCounter24049213 = new Ya.Metrika({
          id: 24049213,
          webvisor: true,
          clickmap: true,
          trackLinks: true,
          accurateTrackBounce: true
        });
      }
    } catch (e) {}
  });
  var n = d.getElementsByTagName("script")[0],
    s = d.createElement("script"),
    f = function() {
      n.parentNode.insertBefore(s, n);
    };
  s.type = "text/javascript";
  s.async = true;
  s.src = (d.location.protocol == "https:" ? "https:" : "http:") + "//mc.yandex.ru/metrika/watch.js";
  if (w.opera == "[object Opera]") {
    d.addEventListener("DOMContentLoaded", f, false);
  } else {
    f();
  }