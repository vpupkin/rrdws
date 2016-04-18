function (w, d, n, s, t) {
      w[n] = w[n] || [];
      w[n].push(function() {
        Ya.Direct.insertInto(149298, "yandex_ad", {
          stat_id: 5,
          ad_format: "direct",
          font_family: "verdana",
          type: "posterVertical",
          border_type: "block",
          limit: 2,
          title_font_size: 3,
          border_radius: true,
          links_underline: false,
          site_bg_color: "FFFFFF",
          border_color: "FBE5C0",
          title_color: "0066CC",
          url_color: "006600",
          text_color: "000000",
          hover_color: "0066FF",
          sitelinks_color: "0066CC",
          favicon: true,
          no_sitelinks: false
        });
      });
      t = d.getElementsByTagName("script")[0];
      s = d.createElement("script");
      s.src = "//an.yandex.ru/system/context.js";
      s.type = "text/javascript";
      s.async = true;
      t.parentNode.insertBefore(s, t);