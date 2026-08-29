(function(){
var toggle = document.querySelector('.nav-toggle');
var menu = document.querySelector('.nav-menu');
var overlay = document.querySelector('.nav-overlay');
if (!toggle || !menu || !overlay) return;

function close() {
  toggle.classList.remove('open');
  menu.classList.remove('open');
  overlay.classList.remove('open');
}

toggle.addEventListener('click', function() {
  var open = toggle.classList.toggle('open');
  menu.classList.toggle('open', open);
  overlay.classList.toggle('open', open);
});

overlay.addEventListener('click', close);

menu.querySelectorAll('a').forEach(function(a) {
  a.addEventListener('click', close);
});
})();
