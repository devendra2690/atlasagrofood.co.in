document.addEventListener("DOMContentLoaded", function () {
  // Track WhatsApp CTA clicks
  document.querySelectorAll('a[href*="wa.me"], a[href*="whatsapp"]').forEach(function(el){
    el.addEventListener("click", function(){
      console.log("WhatsApp CTA clicked:", el.href);
    });
  });

  // Track Quote / Contact CTA clicks
  document.querySelectorAll('a[href*="quote"], a[href*="contact"]').forEach(function(el){
    el.addEventListener("click", function(){
      console.log("Quote/Contact CTA clicked:", el.href);
    });
  });
});
