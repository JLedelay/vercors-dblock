/*
 * Copyright 2012-2016 École polytechnique fédérale de Lausanne (EPFL), Switzerland
 * Copyright 2012-2016 Crossing-Tech SA, Switzerland
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Author: Simon Bliudze, Anastasia Mavridou, Radoslaw Szymanek and Alina Zolotukhina
 * Date: 15.10.12
 */

package org.javabip.annotations;

// import java.lang.annotation.Retention;
// import java.lang.annotation.RetentionPolicy;

/**
 * It annotates the function with implementing a guard. The function must return a boolean value.
 * 
 * @author Alina Zolotukhina
 */
// Retention(RetentionPolicy.RUNTIME)
public /*@ bip_annotation @*/ @interface Guard {

	/**
	 * It returns the name of the guard.
	 * 
	 * @return the name of the guard.
	 */
	String name();
}
