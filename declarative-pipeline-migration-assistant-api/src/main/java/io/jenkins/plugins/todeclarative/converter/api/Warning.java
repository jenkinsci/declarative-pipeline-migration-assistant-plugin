package io.jenkins.plugins.todeclarative.converter.api;

import edu.umd.cs.findbugs.annotations.CheckForNull;

public class Warning
{
    private String message;

    private Class typeClass;

    public Warning()
    {
        // nop
    }

    public Warning( String message, Class typeClass)
    {
        this.message = message;
        this.typeClass = typeClass;
    }

    public String getMessage()
    {
        return message;
    }

    public void setMessage( String message )
    {
        this.message = message;
    }

    public void setTypeClass(Class typeClass)
    {
        this.typeClass = typeClass;
    }

    @CheckForNull
    public Class getTypeClass()
    {
        return typeClass;
    }

    public String getTypeName()
    {
        if (typeClass != null)
        {
            return typeClass.getName();
        } else
        {
            return "";
        }
    }

    @Override
    public String toString()
    {
        return "Warning{" + "message='" + message + '\'' + ", typeClassName='" + getTypeName() + '\'' + '}';
    }
}
