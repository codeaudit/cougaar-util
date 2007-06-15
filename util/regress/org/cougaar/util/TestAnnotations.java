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
    public int simple;

    @Arguments.Spec(
	    name = "SimpleDefaultedParam", 
	    defaultValue="10",
	    required=false,
	    valueType=Arguments.BaseDataType.FIXED
    )
    public int simpleDefaulted;

    @Arguments.Spec(
	    name = "SimpleDefaultedNullParam", 
	    defaultValue=Arguments.NULL_VALUE,
	    required=false,
	    valueType=Arguments.BaseDataType.STRING
    )
    public String simpleDefaultedNull;

    @Arguments.Spec(
	    name = "ListParam", 
	    sequence=true
    )
    public List<String> list;

    @Arguments.Spec(
	    name = "DefaultedListParam", 
	    sequence=true,
	    defaultValue="[d,e]",
	    required=false
    )
    public List<String> defaultedList;

    @Arguments.Spec(
	    name = "DefaultedListNullParam", 
	    sequence=true,
	    defaultValue=Arguments.NULL_VALUE,
	    required=false
    )
    public List<String> defaultedListNull;

    private void setAll(Arguments args) {
	try {
	    args.setAllFields(this);
	} catch (Exception e) {
	    e.printStackTrace();
	    fail(e.getMessage());
	}
    }
    
    private void setGroup(List<Arguments> owner, int index) {
	try {
	    owner.get(index).setFields(this);
	} catch (Exception e) {
	    e.printStackTrace();
	    fail(e.getMessage());
	}
    }
    
    protected void setUp() {
	simple = -1;
	simpleDefaulted = -1;
	simpleDefaultedNull = "simpleDefaultedNull";
	list = null;
	defaultedList = null;
	defaultedListNull = new ArrayList<String>();
    }

    public void test_values() {
	assertEquals(simple, -1);
	assertNull(list);
	Arguments arguments = new Arguments(PARAMS);
	setAll(arguments);
	assertEquals(simple, 1);
	assertNotNull(list);
	assertEquals(list.size(), 2);
	assertEquals(list.get(0), "a");
	assertEquals(list.get(1), "b");
    }
    
    public void test_defaults() {
	assertEquals(simpleDefaulted, -1);
	assertEquals(simpleDefaultedNull, "simpleDefaultedNull");
	assertNull(defaultedList);
	assertNotNull(defaultedListNull);
	Arguments arguments = new Arguments(PARAMS);
	setAll(arguments);
	assertEquals(simpleDefaulted, 10);
	assertNull(simpleDefaultedNull);
	assertNotNull(defaultedList);
	assertEquals(defaultedList.size(), 2);
	assertEquals(defaultedList.get(0), "d");
	assertEquals(defaultedList.get(1), "e");
	assertNull(defaultedListNull);
    }

    public void test_groups() {
	assertNull(groupOwner);
	Arguments arguments = new Arguments(PARAMS);
	setAll(arguments);
	assertNotNull(groupOwner);
	assertEquals(groupOwner.size(), 2);
	setGroup(groupOwner, 1);
	assertEquals(simple, 100);
	setGroup(groupOwner, 0);
	assertEquals(simple, 1);
    }
}