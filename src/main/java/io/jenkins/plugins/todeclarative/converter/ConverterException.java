package io.jenkins.plugins.todeclarative.converter;

public class ConverterException extends Exception
{
    public ConverterException( String message, Throwable cause )
    {
        super( message, cause );
    }

    public ConverterException( Throwable cause )
    {
        super( cause );
    }
}
