Prism.plugins.toolbar.registerButton('download-jenkinsfile', {
  text: 'Download', // required
  onClick: function (env) { // optional
    var element = document.createElement('a');
    element.setAttribute('href', 'data:text/plain;charset=utf-8,' + encodeURIComponent(env.code));
    element.setAttribute('download', "Jenkinsfile");
    element.style.display = 'none';
    document.body.appendChild(element);
    element.click();
    document.body.removeChild(element);
  }
});

// $("#jenkinsfile-content").hide();
