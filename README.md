## Declarative Pipeline Migration Assistant 

[![Jenkins Plugin](https://img.shields.io/jenkins/plugin/v/declarative-pipeline-migration-assistant)](https://plugins.jenkins.io/declarative-pipeline-migration-assistant)
[![Changelog](https://img.shields.io/github/v/tag/jenkinsci/convert-to-declarative?label=changelog)](https://github.com/jenkinsci/declarative-pipeline-migration-assistant-plugin/releases)
[![Jenkins Plugin Installs](https://img.shields.io/jenkins/plugin/i/declarative-pipeline-migration-assistant?color=blue)](https://plugins.jenkins.io/declarative-pipeline-migration-assistant)
[![Contributors](https://img.shields.io/github/contributors/jenkinsci/convert-to-declarative.svg)](https://github.com/jenkinsci/declarative-pipeline-migration-assistant-plugin/graphs/contributors)

This project includes a plugin that uses details from a Freestyle project to generate a starting Jenkinsfile. The Declarative Pipeline Migration Assistant plugin uses a “best effort” approach during generation, which means supported configurations in Freestyle projects will be automatically converted, and placeholder stages will be created for plugins that are not yet supported.

There are two modules in the project:
- The plugin which uses the API to generate a Jenkinsfile based on a Freestyle project
- The API (the base of extension points to convert different parts of a Freestyle project)

Documentation for this plugin is hosted on 
[the CloudBees documentation site](https://docs.cloudbees.com/docs/admin-resources/latest/pipelines-user-guide/declarative-pipeline-migration-assistant).

### The plugin
For further details on using the plugin, please see Converting a Freestyle project to a Declarative Pipeline.

### API 
The abstract class `SingleTypedConverter<T>` defines the extension of the conversion.

The Declarative Pipeline Migration Assistant plugin currently supports a limited number of plugins. See the documentation for details. 

If you want to add support for a specific plugin that is not currently supported, the process includes the following steps:

#### Add converter api dependency

```

    <dependency>
      <groupId>org.jenkins-ci.plugins.to-declarative</groupId>
      <artifactId>declarative-pipeline-migration-assistant-api</artifactId>
      <version></version>  
    </dependency>

``` 
 
#### Create your extension

```
@Extension
public class ShellConverter extends SingleTypedConverter<Shell>
    @Override
    public boolean convert( ConverterRequest request, ConverterResult result, Object target )
    {
       Shell shell = (Shell) target;
       /* Do some work to generate a new stage... */
       ModelASTStage stage = new ModelASTStage( this );

       // use an utility method to add the stage to the pipeline model
       ModelASTUtils.addStage(request.getModelASTPipelineDef(), stage);

       // true for success, false for failure
       return true;
    }
```

#### Example Build Step conversion

The following is an example with comments to convert a Shell script freestyle step using the API.

```
@Extension
public class ShellConverter extends SingleTypedConverter<Shell>
{
    @Override
    public boolean convert( ConverterRequest request, ConverterResult result, Object target )
    {
        Shell shell = (Shell) target;
        ModelASTStage stage = new ModelASTStage( this );
        int stageNumber = request.getAndIncrement( SHELL_NUMBER_KEY );
        // names need to be unique for this we use a counter internal to the current conversion
        stage.setName( "Shell script " + stageNumber );
        // we create a branch of the pipeline 
        ModelASTBranch branch = new ModelASTBranch( this );
        // we add it to the returned stage    
        stage.setBranches( Arrays.asList( branch ) );
        // the step doing the job
        ModelASTStep step = new ModelASTStep( this );
        // the used pipeline function 
        step.setName( "sh" );
        ModelASTSingleArgument singleArgument = new ModelASTSingleArgument( this );
        // we add the argument(s) coming from the freestyle build step configuration
        singleArgument.setValue( ModelASTValue.fromConstant( shell.getCommand(), this ) );
        step.setArgs( singleArgument );
        // we used some helper methods as we may have some wrapper around steps such credential, timeout, configfile etc...
        ModelASTUtils.wrapBranch(result, step, branch);
        // use an utility method to add the stage to the pipeline model
        ModelASTUtils.addStage(result.getModelASTPipelineDef(), stage);

        return true;
    }
}
```

#### Example Publisher conversion

The following is an example with comments to convert the ArtifactArchiver freestyle post build step using the API.
This conversion modifies the model to add some build conditions.

```
@Extension
public class ArtifactArchiverConverter extends SingleTypedConverter<ArtifactArchiver> {
    @Override
    public boolean convert(ConverterRequest request, ConverterResult result, Object target) {

        ArtifactArchiver artifactArchiver = (ArtifactArchiver) target;
        ModelASTBuildCondition buildCondition;
        // depending on which condition the artifact need to be executed
        if(artifactArchiver.isOnlyIfSuccessful()) {
            buildCondition = ModelASTUtils.buildOrFindBuildCondition( result.getModelASTPipelineDef(), "success" );
        } else {
            buildCondition = ModelASTUtils.buildOrFindBuildCondition( result.getModelASTPipelineDef(), "always" );
        }

        // we use the helping method for basic generic publisher
        // NOTE your Publisher may not be able to be converted with this
        // in this case you need to code manually the step
        ModelASTStep archiveArtifacts = ModelASTUtils.buildGenericStep(artifactArchiver, this);
        // we add the step to the build condition
        ModelASTUtils.addStep(buildCondition, archiveArtifacts);

        return true;
    }
}

``` 


#### Example Build Wrapper conversion

The following is an example with comments to convert the Config File freestyle wrapper build using the API.
This conversion uses a helper method to add a wrapper around all future build step conversions.

```
// This was to not have the config-file-provider plugin as a required dependency
// But you can use (as your use your plugin) 
@OptionalExtension(requirePlugins = { "config-file-provider" })
public class ConfigFileBuildWrapperConverter extends SingleTypedConverter<ConfigFileBuildWrapper>
{
    private Logger LOGGER = LoggerFactory.getLogger( ConfigFileBuildWrapperConverter.class );

    @Override
    public boolean convert(ConverterRequest request, ConverterResult result, Object target)
    {
        ConfigFileBuildWrapper configFileBuildWrapper = (ConfigFileBuildWrapper) target;
        if(configFileBuildWrapper.getManagedFiles() == null || configFileBuildWrapper.getManagedFiles().isEmpty() )
        {
            return true;
        }
        // return a lambda which will be called to wrap build step branches conversion 
        // see Build Step conversion which use it
        result.addWrappingTreeStep( () -> build( configFileBuildWrapper ) );
        return true;
    }

    private ModelASTTreeStep build(ConfigFileBuildWrapper configFileBuildWrapper) {
        ModelASTTreeStep configFileProvider = new ModelASTTreeStep( this );

        configFileProvider.setName( "configFileProvider" );
        ModelASTSingleArgument singleArgument = new ModelASTSingleArgument( null);
        configFileProvider.setArgs( singleArgument );

        // only the 1st one
        ManagedFile managedFile = configFileBuildWrapper.getManagedFiles().get( 0 );
        // we simply generate to groovy code
        StringBuilder gstring = new StringBuilder( "[configFile(fileId:'" );
        gstring.append( managedFile.getFileId());
        gstring.append( "', targetLocation: '" );
        gstring.append( managedFile.getTargetLocation() );
        gstring.append( "')]" );
        singleArgument.setValue( ModelASTValue.fromGString( gstring.toString(), this ) );

        return configFileProvider;
    }
}

``` 

#### Example SCM conversion

The following is an example with comments to convert the Git SCM freestyle stage using the API.
This conversion adds a stage to the pipeline model.

```
@OptionalExtension(requirePlugins = { "git" })
public class GitScmConverter extends SingleTypedConverter<GitSCM>
{
    @Override
    public boolean convert(ConverterRequest request, ConverterResult result, Object target)
    {
        List<UserRemoteConfig> repoList = ( (GitSCM) target ).getUserRemoteConfigs();
        if(repoList.isEmpty()){
            return true;
        }
        // create the new stage
        ModelASTStage stage = new ModelASTStage( this );
        stage.setName( "Checkout Scm" );
        // what will be generated as step
        //git url: "", branch: '',changelog: '', credentialsId: '', pool: ''
        List<ModelASTStep> steps = new ArrayList<>();
        // a step will be created per remote repository
        for( UserRemoteConfig userRemoteConfig : repoList)
        {
            // create the git step 
            ModelASTStep git = new ModelASTStep( null );
            git.setName( "git" );

            Map<ModelASTKey, ModelASTValue> args = new HashMap<>();
            // add parameters
            { // url
                ModelASTKey url = new ModelASTKey( this );
                url.setKey( "url" );
                ModelASTValue urlValue = ModelASTValue.fromConstant( userRemoteConfig.getUrl(), this );
                args.put( url, urlValue );
            }

            // more parameters in the original code
            ...

            // configure args of the step
            ModelASTNamedArgumentList stepArgs = new ModelASTNamedArgumentList( null);
            stepArgs.setArguments( args );
            git.setArgs( stepArgs );
            steps.add( git );
        }

        // create a branch for the stage
        ModelASTBranch branch = new ModelASTBranch( this );
        branch.setSteps(steps);
        stage.setBranches( Arrays.asList( branch ) );
        // use an utility method to add the stage to the pipeline model
        ModelASTUtils.addStage(result.getModelASTPipelineDef(), stage );
        return true;
    }
}
```

#### Example Build Trigger conversion

The following is an example with comments to convert the cron trigger using the API.
This conversion modifies the pipeline mode to add a trigger property via a utility method.

```
@Extension
public class TimerTriggerConverter extends SingleTypedConverter<TimerTrigger>
{
    @Override
    public boolean convert(ConverterRequest request, ConverterResult result, Object target)
    {
        TimerTrigger timerTrigger = (TimerTrigger) target;

        String cronValue = timerTrigger.getSpec();
        // simply create the cron option 

        ModelASTTrigger modelASTTrigger = new ModelASTTrigger( this );
        modelASTTrigger.setName( "cron" );
        modelASTTrigger.setArgs( Arrays.asList(ModelASTValue.fromConstant( cronValue, this )) );

        // add the option to the model
        ModelASTUtils.addTrigger( result.getModelASTPipelineDef(), modelASTTrigger );
        return true;
    }
}

```

