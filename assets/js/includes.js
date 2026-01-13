// assets/js/includes.js
document.addEventListener("DOMContentLoaded", function() {
    
    // Load Header
    fetch('/includes/header.html')
        .then(response => response.text())
        .then(data => {
            document.getElementById('site-header').innerHTML = data;
        });

    // Load Footer
    fetch('/includes/footer.html')
        .then(response => response.text())
        .then(data => {
            document.getElementById('site-footer').innerHTML = data;
            
            // Dynamic Year Update
            const yearSpan = document.getElementById('footer-year');
            if(yearSpan) yearSpan.textContent = new Date().getFullYear();
        });
});

/* =========================================
   Global Mobile Menu Logic (Centralized)
   ========================================= */
document.addEventListener('DOMContentLoaded', () => {
    
    // We use "Event Delegation" here.
    // Instead of looking for the button (which might not be loaded yet),
    // we listen to the 'body' and check if the clicked element matches our ID.
    
    document.body.addEventListener('click', (event) => {
        
        // 1. Check if user clicked the "Open Menu" button (or the SVG icon inside it)
        const openBtn = event.target.closest('#mobile-open-btn');
        if (openBtn) {
            event.preventDefault(); // Stop page jump
            document.getElementById('mobile-overlay').classList.remove('hidden');
            document.getElementById('mobile-sidebar').classList.remove('hidden');
        }

        // 2. Check if user clicked the "Close" button (X) OR the dark Overlay
        const closeBtn = event.target.closest('#mobile-close-btn');
        const overlay = event.target.closest('#mobile-overlay'); // Clicking the dark background

        if (closeBtn || (overlay && event.target.id === 'mobile-overlay')) {
            event.preventDefault();
            document.getElementById('mobile-overlay').classList.add('hidden');
            document.getElementById('mobile-sidebar').classList.add('hidden');
        }
    });
});