/*
 * <copyright>
 *  
 *  Copyright 1997-2007 BBNT Solutions, LLC
 *  under sponsorship of the Defense Advanced Research Projects
 *  Agency (DARPA).
 * 
 *  You can redistribute this software and/or modify it under the
 *  terms of the Cougaar Open Source License as published on the
 *  Cougaar Open Source Website (www.cougaar.org).
 * 
 *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 *  "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 *  LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 *  A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 *  OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 *  SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 *  LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 *  DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 *  THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 *  OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *  
 * </copyright>
 */

package org.cougaar.util;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.cougaar.util.Arguments.GroupIterationPolicy;
import org.cougaar.util.Arguments.GroupRole;

/**
 * Test field initialization via annotation metatdata
 * @author rshapiro
 *
 */
public class TestAnnotations extends TestCase {
    private static final String TEST_GROUP_NAME = "TestArgGroup";
    private static final String PARAMS = "SimpleParam=1, SimpleParam=100, ListParam=a, ListParam=b ";

    @Retention(RetentionPolicy.RUNTIME)
    public @interface TestArgGroup {
	String name() default TEST_GROUP_NAME;
	GroupRole role() default GroupRole.MEMBER;
	GroupIterationPolicy policy() default GroupIterationPolicy.FIRST_UP;
    }

    @TestArgGroup(role=Arguments.GroupRole.OWNER)
    public List<Arguments> groupOwner;

    @Arguments.Spec(
	    name = "SimpleParam", 
	    valueType=Arguments.BaseDataType.FIXED
    )
    @TestArgGroup()
    public int simple = -1;

    @Arguments.Spec(
	    name = "SimpleDefaultedParam", 
	    defaultValue="10",
	    required=false,
	    valueType=Arguments.BaseDataType.FIXED
    )
    public int simpleDefaulted = -1;

    @Arguments.Spec(
	    name = "SimpleDefaultedNullParam", 
	    defaultValue=Arguments.NULL_VALUE,
	    required=false,
	    valueType=Arguments.BaseDataType.STRING
    )
    public String simpleDefaultedNull = "simpleDefaultedNull";

    @Arguments.Spec(
	    name = "ListParam", 
	    sequence=true
    )
    public List<String> list = null;

    @Arguments.Spec(
	    name = "DefaultedListParam", 
	    sequence=true,
	    defaultValue="[d,e]",
	    required=false
    )
    public List<String> defaultedList = null;

    @Arguments.Spec(
	    name = "DefaultedListNullParam", 
	    sequence=true,
	    defaultValue=Arguments.NULL_VALUE,
	    required=false
    )
    public List<String> defaultedListNull = new ArrayList<String>();

    public void test_preconditions() {
	assertEquals(simple, -1);
	assertEquals(simpleDefaulted, -1);
	assertEquals(simpleDefaultedNull, "simpleDefaultedNull");
	assertNull(list);
	assertNull(defaultedList);
	assertNotNull(defaultedListNull);
    }

    private void setAllFields(Arguments a1) {
	try {
	    a1.setAllFields(this);
	} catch (Exception e) {
	    e.printStackTrace();
	    fail(e.getMessage());
	}
    }

    public void test_postconditions() {
	Arguments arguments = new Arguments(PARAMS);
	setAllFields(arguments);
	assertEquals(simple, 1);
	assertEquals(simpleDefaulted, 10);
	assertNull(simpleDefaultedNull);
	assertNotNull(list);
	assertEquals(list.size(), 2);
	assertEquals(list.get(0), "a");
	assertEquals(list.get(1), "b");
	assertNotNull(defaultedList);
	assertEquals(defaultedList.size(), 2);
	assertEquals(defaultedList.get(0), "d");
	assertEquals(defaultedList.get(1), "e");
	assertNull(defaultedListNull);
    }

    public void test_groups() {
	Arguments arguments = new Arguments(PARAMS);
	assertNull(groupOwner);
	setAllFields(arguments);
	assertNotNull(groupOwner);
	assertEquals(groupOwner.size(), 2);
	try {
	    groupOwner.get(1).setFields(this);
	} catch (Exception e) {
	    e.printStackTrace();
	    fail(e.getMessage());
	}
	assertEquals(simple, 100);
	try {
	    groupOwner.get(0).setFields(this);
	} catch (Exception e) {
	    e.printStackTrace();
	    fail(e.getMessage());
	}
	assertEquals(simple, 1);
    }
}