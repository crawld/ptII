package ptolemy.domains.ct.demo.Corba.util;

/**
* ptolemy/domains/ct/demo/Corba/util/CorbaIllegalValueExceptionHolder.java
* Generated by the IDL-to-Java compiler (portable), version "3.0"
* from CorbaActor.idl
* Thursday, January 18, 2001 5:51:19 PM PST
*/

public final class CorbaIllegalValueExceptionHolder implements org.omg.CORBA.portable.Streamable
{
    public ptolemy.domains.ct.demo.Corba.util.CorbaIllegalValueException value = null;

    public CorbaIllegalValueExceptionHolder ()
    {
    }

    public CorbaIllegalValueExceptionHolder (ptolemy.domains.ct.demo.Corba.util.CorbaIllegalValueException initialValue)
    {
        value = initialValue;
    }

    public void _read (org.omg.CORBA.portable.InputStream i)
    {
        value = ptolemy.domains.ct.demo.Corba.util.CorbaIllegalValueExceptionHelper.read (i);
    }

    public void _write (org.omg.CORBA.portable.OutputStream o)
    {
        ptolemy.domains.ct.demo.Corba.util.CorbaIllegalValueExceptionHelper.write (o, value);
    }

    public org.omg.CORBA.TypeCode _type ()
    {
        return ptolemy.domains.ct.demo.Corba.util.CorbaIllegalValueExceptionHelper.type ();
    }

}
