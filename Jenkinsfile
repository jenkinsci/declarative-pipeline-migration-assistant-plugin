// TODO: Run tests on Windows
// NOTE: Cannot use ACI agents because build requires `unzip` to be available
buildPlugin(configurations: [
  [ platform: 'linux', jdk: '8', jenkins: null ],
  [ platform: 'linux', jdk: '8', jenkins: '2.164.1', javaLevel: '8' ]])
  // TODO: Some test tries to build a Maven project with source 1.5, and 1.6 is the oldest support by Java 11 apparently.
  //[ platform: 'linux', jdk: '11', jenkins: '2.164.1', javaLevel: '8' ])
