package io.jenkins.plugins.todeclarative.converter.api;

public class Warning
{
    private String message;

    private String pluginClassName;

    public Warning()
    {
        // nop
    }

    public Warning( String message, String pluginClassName )
    {
        this.message = message;
        this.pluginClassName = pluginClassName;
    }

    public String getMessage()
    {
        return message;
    }

    public void setMessage( String message )
    {
        this.message = message;
    }

    public String getPluginClassName()
    {
        return pluginClassName;
    }

    public void setPluginClassName( String pluginClassName )
    {
        this.pluginClassName = pluginClassName;
    }

    @Override
    public String toString()
    {
        return "Warning{" + "message='" + message + '\'' + ", pluginClassName='" + pluginClassName + '\'' + '}';
    }
}
