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
       return the stage corresponding to the conversion 
    }

    @Override
    public boolean canConvert( Builder builder )
    {
        return true if your implementation is able to convert the Builder passed as a parameter  
    }
```

### Plugin 

