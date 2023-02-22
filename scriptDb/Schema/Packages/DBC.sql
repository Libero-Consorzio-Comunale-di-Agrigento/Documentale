CREATE OR REPLACE package DbC is
/******************************************************************************
 NAME:        DbC.
 DESCRIPTION: Machinery to support Design-by-Contract.
 ANNOTATIONS: .
 REVISION: .
 <CODE>
 Rev.  Date        Author  Description
 00    16/03/2005  CZecca  First release.
 01    03/01/2006  CZecca  handling of clauses which evaluate to null; version and revision
 02    12/04/2006  FT      sobstitution of ampersand character with token 'and' in revision
                           01 to solve problems during the package execution in SQL*Plus
 03    30/08/2006  FT      Modifica dichiarazione subtype per incompatibilit¿ con
                           versione 7 di Oracle
 </CODE>
******************************************************************************/

   d_revision varchar2(30);
   subtype t_revision is d_revision%type;
   s_revisione constant t_revision := 'V1.03';

   function versione return t_revision;
   pragma restrict_references( versione, WNDS, WNPS );

   pragma restrict_references( DbC, WNDS );

   -- {%skip} pragma exception_init does NOT allow use of symbolic constants

   -- Type of numeric error codes associated to the exceptions
   subtype t_error_number is binary_integer;

   -- Diagnostics for the precondition violations
   precondition_violation           exception;
   precondition_number              constant t_error_number := -20101;
   pragma exception_init( precondition_violation, -20101 );

   -- Diagnostics for the postcondition violations
   postcondition_violation          exception;
   postcondition_number             constant number := -20102;
   pragma exception_init( postcondition_violation, -20102 );

   -- Diagnostics for the assertion violations
   assertion_violation              exception;
   assertion_number                 constant number := -20103;
   pragma exception_init( assertion_violation, -20103 );

   -- Diagnostics for the invariant violations
   invariant_violation              exception;
   invariant_number                 constant number := -20104;
   pragma exception_init( invariant_violation, -20104 );


   -- To check precondition clauses
   procedure PRE
   ( p_condition in boolean
   , p_message in varchar2 := null
   );
   pragma restrict_references( PRE, WNDS );

   -- To know if check of precondition clauses is on
   function pre_on
   return number;
   pragma restrict_references( pre_on, WNDS );
   function PreOn
   return boolean;
   pragma restrict_references( PreOn, WNDS );

   -- To switch on/off the check of precondition clauses
   procedure pre_set
   ( p_on in number
   );
   procedure PreSet
   ( p_on in boolean
   );


   -- To check postcondition clauses
   procedure POST
   ( p_condition in boolean
   , p_message in varchar2 := null
   );
   pragma restrict_references( POST, WNDS );

   -- To know if check of postcondition clauses is on
   function post_on
   return number;
   pragma restrict_references( post_on, WNDS );
   function PostOn
   return boolean;
   pragma restrict_references( PostOn, WNDS );

   -- To switch on/off the check of postcondition clauses
   procedure post_set
   ( p_on in number
   );
   procedure PostSet
   ( p_on in boolean
   );


   -- To check assertion clauses
   procedure ASSERTION
   ( p_condition in boolean
   , p_message in varchar2 := null
   );
   pragma restrict_references( ASSERTION, WNDS );

   -- To know if check of assertion clauses is on
   function assertion_on
   return number;
   pragma restrict_references( assertion_on, WNDS );
   function AssertionOn
   return boolean;
   pragma restrict_references( AssertionOn, WNDS );

   -- To switch on/off the check of assertion clauses
   procedure assertion_set
   ( p_on in number
   );
   procedure AssertionSet
   ( p_on in boolean
   );


   -- To check invariant clauses
   procedure INVARIANT
   ( p_condition in boolean
   , p_message in varchar2 := null
   );
   pragma restrict_references( INVARIANT, WNDS );

   -- To know if check of invariant clauses is on
   function invariant_on
   return number;
   pragma restrict_references( invariant_on, WNDS );
   function InvariantOn
   return boolean;
   pragma restrict_references( InvariantOn, WNDS );

   -- To switch on/off the check of invariant clauses
   procedure invariant_set
   ( p_on in number
   );
   procedure InvariantSet
   ( p_on in boolean
   );

end DbC;
/

