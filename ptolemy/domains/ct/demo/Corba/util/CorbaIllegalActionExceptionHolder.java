package ptolemy.domains.ct.demo.Corba.util;


/**
 * ptolemy/domains/ct/demo/Corba/util/CorbaIllegalActionExceptionHolder.java
 * Generated by the IDL-to-Java compiler (portable), version "3.0"
 * from CorbaActor.idl
 * Thursday, January 18, 2001 5:51:18 PM PST
 */
public final class CorbaIllegalActionExceptionHolder
    implements org.omg.CORBA.portable.Streamable {
    public ptolemy.domains.ct.demo.Corba.util.CorbaIllegalActionException value = null;

    public CorbaIllegalActionExceptionHolder() {
    }

    public CorbaIllegalActionExceptionHolder(
        ptolemy.domains.ct.demo.Corba.util.CorbaIllegalActionException initialValue) {
        value = initialValue;
    }

    public void _read(org.omg.CORBA.portable.InputStream i) {
        value = ptolemy.domains.ct.demo.Corba.util.CorbaIllegalActionExceptionHelper
                        .read(i);
    }

    public void _write(org.omg.CORBA.portable.OutputStream o) {
        ptolemy.domains.ct.demo.Corba.util.CorbaIllegalActionExceptionHelper
                    .write(o, value);
    }

    public org.omg.CORBA.TypeCode _type() {
        return ptolemy.domains.ct.demo.Corba.util.CorbaIllegalActionExceptionHelper
                    .type();
    }
}
