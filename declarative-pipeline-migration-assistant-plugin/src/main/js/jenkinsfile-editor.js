var $ = require('jqueryui-detached').getJQueryUI();
var jenkinsJSModules = require('jenkins-js-modules');
var editorIdCounter = 0;

// The Jenkins 'ace-editor:ace-editor-122' plugin doesn't support a synchronous 
// require option. This is because of how the ACE editor is written. So, we need
// to use lower level jenkins-js-modules async 'import' to get a handle on a 
// specific version of ACE, from which we create an editor instance for workflow. 
jenkinsJSModules.import('ace-editor:ace-editor-122')
    .onFulfilled(function (acePack) {
        
        $('.jenkinsfile-editor-wrapper').each(function() {
            initEditor($(this));        
        });

        $('.rectangle-copy').on('click', function() {
            var editor = window.aceEditor;
            var sel = editor.selection.toJSON();
            editor.selectAll();
            editor.focus();
            document.execCommand('copy');
            editor.selection.fromJSON(sel);
        });

        $('.rectangle-copy-modal').on('click', function() {
          var editor = window.aceEditorCopy;
          var sel = editor.selection.toJSON();
          editor.selectAll();
          editor.focus();
          document.execCommand('copy');
          editor.selection.fromJSON(sel);
        });

        var wWidth = $(window).width();
        var dWidth = wWidth * 0.8;

        var wHeight = $(window).height();
        var dHeight = wHeight * 0.8;

        dialog = $('#modal').dialog({
                                      autoOpen: false,
                                      dialogClass: "no-close",
                                      width: dWidth,
                                      height: dHeight,
                                      modal: true
                                    });

        $('.rectangle-download').on('click', function() {
          download('Jenkinsfile',$('#jenkinsfile-content').val());
        });

      $('.rectangle-expand').on('click', function() {
        $("#jenkinsfile-editor-original").hide();
        $(".review-converted-top-title").hide();
        dialog.dialog( "open" );
        $('#jenkinsfile-editor-copy').each(function() {
          initEditor($(this));
        });
        window.aceEditor.css('opacity', 0);
        window.aceEditorCopy.css('opacity','1');
        window.aceEditorCopy.resize(true);
      });

        $('.rectangle-minimize').on('click', function() {
          dialog.dialog( "close" );
          // $('.jenkinsfile-editor-wrapper').each(function() {
          //   initEditor($(this));
          // });
          $(".review-converted-top-title").show();
          $("#jenkinsfile-editor-original").show();
        });


        function initEditor(wrapper) {

            var textarea = $('textarea', wrapper);
            var aceContainer = $('.editor', wrapper);
            
            $('.textarea-handle', wrapper).remove();
            
            // The ACE Editor js expects the container element to have an id.
            // We generate one and add it.
            editorIdCounter++;
            var editorId = 'jenkinsfile-editor-' + editorIdCounter;
            aceContainer.attr('id', editorId);
            
            // The 'ace-editor:ace-editor-122' plugin supplies an "ACEPack" object.
            // ACEPack understands the hardwired async nature of the ACE impl and so
            // provides some async ACE script loading functions.
            
            acePack.edit(editorId, function() {
                var ace = acePack.ace;
                var editor = this.editor;
                
                // Attach the ACE editor instance to the element. Useful for testing.
                //var $wfEditor = $('#' + editorId);
                //$wfEditor.get(0).aceEditor = editor;
                if(window.aceEditor){
                  window.aceEditorCopy = editor;
                } else {
                  window.aceEditor = editor;
                }
    
                acePack.addPackOverride('snippets/groovy.js');
    
                acePack.addScript('ext-language_tools.js', function() {
                    ace.require("ace/ext/language_tools");
                    
                    editor.$blockScrolling = Infinity;
                    editor.session.setMode("ace/mode/groovy");
                    editor.setTheme("ace/theme/tomorrow");
                    editor.setAutoScrollEditorIntoView(true);
                    editor.setOption("minLines", 50);
                    editor.setReadOnly(true);
                    editor.setValue(textarea.val(), 1);
                    editor.getSession().on('change', function() {
                        textarea.val(editor.getValue());
                    });
                });
            });
            
            wrapper.show();
            textarea.hide();
        }
    });

function download(filename, text) {
  var element = document.createElement('a');
  element.setAttribute('href', 'data:text/plain;charset=utf-8,' + encodeURIComponent(text));
  element.setAttribute('download', filename);
  element.style.display = 'none';
  document.body.appendChild(element);
  element.click();
  document.body.removeChild(element);
}
