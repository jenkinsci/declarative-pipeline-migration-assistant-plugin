## To Declarative Converter Plugin 
This project can convert freestyle projects to declarative pipeline.
There is two modules in the project:
- The API (the base of extension points to convert different parts of freestyle project)
- The plugin which uses the API to convert a freestyle project

### API 
Some interfaces define some extension of the conversion:
- BuilderConverter: convert Builder
- BuildWrapperConverter: convert BuildWrapper
- JobPropertyConverter: convert JobProperty
- PublisherConverter: convert Publisher
- ScmConverter: convert SCM

To have a plugin participating to the conversion, just create your extension as it:

```
@Extension
public class ShellConverter implements BuilderConverter
    @Override
    public ModelASTStage convert( ConverterRequest request, ConverterResult converterResult, Builder builder )
    {
       return the stage corresponding to the conversion or modify the model
    }

    @Override
    public boolean canConvert( Builder builder )
    {
        return true if your implementation is able to convert the Builder passed as a parameter  
    }
```

#### Example Build Step conversion

Here the commented code to convert the Shell script freestyle step

```
    public ModelASTStage convert( ConverterRequest request, ConverterResult converterResult, Builder builder )
    {
        Shell shell = (Shell) builder;
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
        wrapBranch(converterResult, step, branch);

        return stage;
    }
```

#### Example Publisher conversion

Here the commented code to convert the ArtifactArchiver freestyle post build step.
This conversion do not return a stage but modify the model to add some build condition.

```

    public ModelASTStage convert( ConverterRequest request, ConverterResult result, Publisher publisher )
    {
        if (!(publisher instanceof ArtifactArchiver )) {
            // can't use automatic conversion
            return null;
        }
        ArtifactArchiver artifactArchiver = (ArtifactArchiver) publisher;
        ModelASTBuildCondition buildCondition;
        // depending on which condition the artifact need to be executed
        if(artifactArchiver.isOnlyIfSuccessful())
        {
            buildCondition = ModelASTUtils.buildOrFindBuildCondition( result.getModelASTPipelineDef(), "success" );
        } else {
            buildCondition = ModelASTUtils.buildOrFindBuildCondition( result.getModelASTPipelineDef(), "always" );
        }

        // we need to ensure there is a branch so a null check
        ModelASTBranch branch = buildCondition.getBranch();
        if(branch==null){
            branch =new ModelASTBranch( this );
            buildCondition.setBranch( branch );
        }
        
        // we use the helping method for basic generic publisher
        // NOTE your Publisher may not be able to be converted with this
        // in this case you need to code manually the step
        ModelASTStep archiveArtifacts = buildGenericStep(publisher);
        branch.getSteps().add( archiveArtifacts );

        return null;
    }

``` 

