// define class name and unique id
//#define FMI2_FUNCTION_PREFIX Linsys_
#include <fmi2TypesPlatform.h>
#include <fmi2Functions.h>
#define MODEL_GUID "{8c4e810f-3df3-4a00-8276-176fa3c9f9e0}"
#include <cstdio>
#include <cstring>
#include <cassert>
#include "sfmi_runtime.h"
using namespace std;
using namespace sfmi;

// define model size
#define NUMBER_OF_STATES 2
#define NUMBER_OF_EVENT_INDICATORS 0
#define NUMBER_OF_REALS 8
#define NUMBER_OF_INTEGERS 0
#define NUMBER_OF_STRINGS 0
#define NUMBER_OF_BOOLEANS 0
#define NUMBER_OF_EXTERNALFUNCTIONS 0

// define variable data for model
#define time (data->Time)
#define $Px$lB1$rB_ 0 
#define $Px$lB1$rB (data->real_vars[0]) 
#define $Px$lB2$rB_ 1 
#define $Px$lB2$rB (data->real_vars[1]) 
#define $P$DER$Px$lB1$rB_ 2 
#define $P$DER$Px$lB1$rB (data->real_vars[2]) 
#define $P$DER$Px$lB2$rB_ 3 
#define $P$DER$Px$lB2$rB (data->real_vars[3]) 
#define $PA$lB1$c1$rB_ 4 
#define $PA$lB1$c1$rB (data->real_vars[4]) 
#define $PA$lB1$c2$rB_ 5 
#define $PA$lB1$c2$rB (data->real_vars[5]) 
#define $PA$lB2$c1$rB_ 6 
#define $PA$lB2$c1$rB (data->real_vars[6]) 
#define $PA$lB2$c2$rB_ 7 
#define $PA$lB2$c2$rB (data->real_vars[7]) 

// define initial state vector as vector of value references
static const fmi2ValueReference STATES[NUMBER_OF_STATES] = { $Px$lB1$rB_, $Px$lB2$rB_ };
static const fmi2ValueReference STATESDERIVATIVES[NUMBER_OF_STATES] = { $P$DER$Px$lB1$rB_, $P$DER$Px$lB2$rB_ };


// equation functions


/*
 equation index: 7
 type: SIMPLE_ASSIGN
 der(x[2]) = A[2,1] * x[1] + A[2,2] * x[2]
 */
static void eqFunction_7(model_data *data)
{
    static const int equationIndexes = 7;
    $P$DER$Px$lB2$rB = (($PA$lB2$c1$rB * $Px$lB1$rB) + ($PA$lB2$c2$rB * $Px$lB2$rB));
}
/*
 equation index: 8
 type: SIMPLE_ASSIGN
 der(x[1]) = A[1,1] * x[1] + A[1,2] * x[2]
 */
static void eqFunction_8(model_data *data)
{
    static const int equationIndexes = 8;
    $P$DER$Px$lB1$rB = (($PA$lB1$c1$rB * $Px$lB1$rB) + ($PA$lB1$c2$rB * $Px$lB2$rB));
}

static void setupEquationGraph(model_data *data)
{
    data->link(eqFunction_7,&$P$DER$Px$lB2$rB);
    data->link(&$PA$lB2$c1$rB,eqFunction_7);
    data->link(&$Px$lB1$rB,eqFunction_7);
    data->link(&$PA$lB2$c2$rB,eqFunction_7);
    data->link(&$Px$lB2$rB,eqFunction_7);
    data->link(eqFunction_8,&$P$DER$Px$lB1$rB);
    data->link(&$PA$lB1$c1$rB,eqFunction_8);
    data->link(&$Px$lB1$rB,eqFunction_8);
    data->link(&$PA$lB1$c2$rB,eqFunction_8);
    data->link(&$Px$lB2$rB,eqFunction_8);
}

// Set values for all variables that define a start value
static void setDefaultStartValues(model_data *comp)
{
    comp->Time = 0.0;
    comp->real_vars[$Px$lB1$rB_] = 0;
    comp->real_vars[$Px$lB2$rB_] = 0;
    comp->real_vars[$P$DER$Px$lB1$rB_] = 0;
    comp->real_vars[$P$DER$Px$lB2$rB_] = 0;
    comp->real_vars[$PA$lB1$c1$rB_] = -0.5;
    comp->real_vars[$PA$lB1$c2$rB_] = 0.0;
    comp->real_vars[$PA$lB2$c1$rB_] = 0.0;
    comp->real_vars[$PA$lB2$c2$rB_] = -1.0;
}


static void updateAll(model_data* data)
{
    eqFunction_7(data);
    eqFunction_8(data);

}
// model exchange functions

const char* fmi2GetTypesPlatform() { return fmi2TypesPlatform; }
const char* fmi2GetVersion() { return fmi2Version; }

fmi2Status fmi2SetDebugLogging(fmi2Component, fmi2Boolean, size_t, const fmi2String*)
{
    return fmi2OK;
}

fmi2Status fmi2SetupExperiment(fmi2Component c, fmi2Boolean, fmi2Real, fmi2Real startTime, fmi2Boolean, fmi2Real)
{
    return fmi2SetTime(c,startTime);
}

fmi2Status fmi2EnterInitializationMode(fmi2Component)
{
    return fmi2OK;
}

fmi2Status fmi2ExitInitializationMode(fmi2Component c)
{
    model_data* data = static_cast<model_data*>(c);
    if (data == NULL) return fmi2Error;
    data->update();
    return fmi2OK;
}

fmi2Status fmi2Terminate(fmi2Component)
{
    return fmi2OK;
}

fmi2Status fmi2Reset(fmi2Component c)
{
    model_data* data = static_cast<model_data*>(c);
    if (data == NULL) return fmi2Error;
    setDefaultStartValues(data);
    updateAll(data);
    return fmi2OK;
}

fmi2Status fmi2GetFMUstate(fmi2Component, fmi2FMUstate*) { return fmi2Error; }
fmi2Status fmi2SetFMUstate(fmi2Component, fmi2FMUstate) { return fmi2Error; }
fmi2Status fmi2FreeFMUstate(fmi2Component, fmi2FMUstate*) { return fmi2Error; }
fmi2Status fmi2SerializedFMUstateSize(fmi2Component, fmi2FMUstate, size_t*) { return fmi2Error; }
fmi2Status fmi2SerializeFMUstate(fmi2Component, fmi2FMUstate, fmi2Byte*, size_t) { return fmi2Error; }
fmi2Status fmi2DeSerializeFMUstate(fmi2Component, const fmi2Byte[], size_t, fmi2FMUstate*) { return fmi2Error; }
fmi2Status fmi2GetDirectionalDerivative(fmi2Component, const fmi2ValueReference*, size_t,
                                                 const fmi2ValueReference*, size_t,
                                                 const fmi2Real*, fmi2Real*) { return fmi2Error; }

fmi2Status fmi2GetDerivatives(fmi2Component c, fmi2Real* der, size_t nvr)
{
    model_data* data = static_cast<model_data*>(c);
    if (data == NULL || nvr > NUMBER_OF_STATES) return fmi2Error;
    for (size_t i = 0; i < nvr; i++) der[i] = data->real_vars[STATESDERIVATIVES[i]];
    return fmi2OK;
}

fmi2Status fmi2GetEventIndicators(fmi2Component, fmi2Real[], size_t)
{
    if (NUMBER_OF_EVENT_INDICATORS == 0) return fmi2OK;
    return fmi2Error;
}

fmi2Status fmi2GetContinuousStates(fmi2Component c, fmi2Real* states, size_t nvr)
{
    model_data* data = static_cast<model_data*>(c);
    if (data == NULL || nvr > NUMBER_OF_STATES) return fmi2Error;
    for (size_t i = 0; i < nvr; i++) states[i] = data->real_vars[STATES[i]];
    return fmi2OK;
}

fmi2Status fmi2SetContinuousStates(fmi2Component c, const fmi2Real* states, size_t nvr)
{
    model_data* data = static_cast<model_data*>(c);
    if (data == NULL || nvr > NUMBER_OF_STATES) return fmi2Error;
    for (size_t i = 0; i < nvr; i++)
    {
        data->real_vars[STATES[i]] = states[i];
        data->modify(&(data->real_vars[STATES[i]]));
    }
    return fmi2OK;
}

fmi2Status fmi2GetNominalsOfContinuousStates(fmi2Component c, fmi2Real* nominals, size_t nvr)
{
    model_data* data = static_cast<model_data*>(c);
    if (data == NULL || nvr > NUMBER_OF_STATES) return fmi2Error;
    for (size_t i = 0; i < nvr; i++) nominals[i] = 1.0;
    return fmi2OK;
}

 fmi2Status fmi2EnterEventMode(fmi2Component)
 {
    if (NUMBER_OF_EVENT_INDICATORS == 0) return fmi2OK;
    return fmi2Error;
 }

 fmi2Status fmi2NewDiscreteStates(fmi2Component, fmi2EventInfo*)
 {
    if (NUMBER_OF_EVENT_INDICATORS == 0) return fmi2OK;
    return fmi2Error;
 }

 fmi2Status fmi2EnterContinuousTimeMode(fmi2Component) { return fmi2OK; }

 fmi2Status fmi2CompletedIntegratorStep(fmi2Component c, fmi2Boolean,
     fmi2Boolean* enterEventMode, fmi2Boolean* terminateSimulation)
 {
    model_data* data = static_cast<model_data*>(c);
    if (data == NULL || enterEventMode == NULL || terminateSimulation == NULL) return fmi2Error;
    data->update();
    *enterEventMode = fmi2False;
    *terminateSimulation = fmi2False;
    return fmi2OK;
 }

fmi2Status
fmi2SetTime(fmi2Component c, fmi2Real t)
{
    model_data* data = static_cast<model_data*>(c);
    if (data == NULL) return fmi2Error;
    data->Time = t;
    data->modify(&(data->Time));
    return fmi2OK;
}

fmi2Status
fmi2GetReal(fmi2Component c, const fmi2ValueReference* vr, size_t nvr, fmi2Real* value)
{
    model_data* data = static_cast<model_data*>(c);
    if (data == NULL) return fmi2Error;
    for (size_t i = 0; i < nvr; i++)
    {
        if (vr[i] >= NUMBER_OF_REALS) return fmi2Error;
        value[i] = data->real_vars[vr[i]];
    }
    return fmi2OK;
}

fmi2Status
fmi2SetReal(fmi2Component c, const fmi2ValueReference* vr, size_t nvr, const fmi2Real* value)
{
    model_data* data = static_cast<model_data*>(c);
    if (data == NULL) return fmi2Error;
    for (size_t i = 0; i < nvr; i++)
    {
        if (vr[i] >= NUMBER_OF_REALS) return fmi2Error;
        data->real_vars[vr[i]] = value[i];
        data->modify((&data->real_vars[vr[i]]));
    }
    return fmi2OK;
}

fmi2Status
fmi2GetInteger(fmi2Component c, const fmi2ValueReference* vr, size_t nvr, fmi2Integer* value)
{
    model_data* data = static_cast<model_data*>(c);
    if (data == NULL) return fmi2Error;
    for (size_t i = 0; i < nvr; i++)
    {
        if (vr[i] >= NUMBER_OF_INTEGERS) return fmi2Error;
        value[i] = data->int_vars[vr[i]];
    }
    return fmi2OK;
}

fmi2Status
fmi2SetInteger(fmi2Component c, const fmi2ValueReference* vr, size_t nvr, const fmi2Integer* value)
{
    model_data* data = static_cast<model_data*>(c);
    if (data == NULL) return fmi2Error;
    for (size_t i = 0; i < nvr; i++)
    {
        if (vr[i] >= NUMBER_OF_INTEGERS) return fmi2Error;
        data->int_vars[vr[i]] = value[i];
        data->modify((&data->int_vars[vr[i]]));
    }
    return fmi2OK;
}

fmi2Status
fmi2GetBoolean(fmi2Component c, const fmi2ValueReference* vr, size_t nvr, fmi2Boolean* value)
{
    model_data* data = static_cast<model_data*>(c);
    if (data == NULL) return fmi2Error;
    for (size_t i = 0; i < nvr; i++)
    {
        if (vr[i] >= NUMBER_OF_BOOLEANS) return fmi2Error;
        value[i] = data->bool_vars[vr[i]];
    }
    return fmi2OK;
}

fmi2Status
fmi2SetBoolean(fmi2Component c, const fmi2ValueReference* vr, size_t nvr, const fmi2Boolean* value)
{
    model_data* data = static_cast<model_data*>(c);
    if (data == NULL) return fmi2Error;
    for (size_t i = 0; i < nvr; i++)
    {
        if (vr[i] >= NUMBER_OF_BOOLEANS) return fmi2Error;
        data->bool_vars[vr[i]] = value[i];
        data->modify((&data->bool_vars[vr[i]]));
    }
    return fmi2OK;
}

fmi2Status
fmi2GetString(fmi2Component c, const fmi2ValueReference* vr, size_t nvr, fmi2String* value)
{
    model_data* data = static_cast<model_data*>(c);
    if (data == NULL) return fmi2Error;
    for (size_t i = 0; i < nvr; i++)
    {
        if (vr[i] >= NUMBER_OF_STRINGS) return fmi2Error;
        value[i] = data->str_vars[vr[i]].c_str();
    }
    return fmi2OK;
}

fmi2Status
fmi2SetString(fmi2Component c, const fmi2ValueReference* vr, size_t nvr, const fmi2String* value)
{
    model_data* data = static_cast<model_data*>(c);
    if (data == NULL) return fmi2Error;
    for (size_t i = 0; i < nvr; i++)
    {
        if (vr[i] >= NUMBER_OF_STRINGS) return fmi2Error;
        data->str_vars[vr[i]] = value[i];
        data->modify((&data->str_vars[vr[i]]));
    }
    return fmi2OK;
}

fmi2Component
fmi2Instantiate(
  fmi2String instanceName,
  fmi2Type fmuType,
  fmi2String fmuGUID,
  fmi2String fmuResourceLocation,
  const fmi2CallbackFunctions* functions,
  fmi2Boolean visible,
  fmi2Boolean loggingOn)
{
    model_data* data = new model_data(
        NUMBER_OF_REALS,NUMBER_OF_INTEGERS,NUMBER_OF_STRINGS,NUMBER_OF_BOOLEANS);
    setupEquationGraph(data);
    setDefaultStartValues(data);
    updateAll(data);
    return static_cast<fmi2Component>(data);
}

void
fmi2FreeInstance(fmi2Component c)
{
    model_data* data = static_cast<model_data*>(c);
    if (data != NULL) delete data;
}

