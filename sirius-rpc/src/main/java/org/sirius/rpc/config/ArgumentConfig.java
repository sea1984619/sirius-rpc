/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sirius.rpc.config;

import java.io.Serializable;

/**
 * The method arguments configuration
 *
 * @export
 */
public class ArgumentConfig extends AbstractIdConfig implements Serializable {

	private static final long serialVersionUID = -2165482463925213595L;

	/**
	 * The argument index: index -1 represents not set
	 */
	private Integer index = -1;

	/**
	 * Argument type
	 */
	private String type;

	/**
	 * Whether the argument is the callback interface
	 */
	private Boolean callback = false;

	private Boolean retry = false;

	private int attempts = 100;

	private int delay = 10000;

	public Boolean getRetry() {
		return retry;
	}

	public ArgumentConfig setRetry(Boolean retry) {
		this.retry = retry;
		return this;
	}

	public int getAttempts() {
		return attempts;
	}

	public ArgumentConfig setAttempts(int attempts) {
		this.attempts = attempts;
		return this;
	}

	public int getDelay() {
		return delay;
	}

	public ArgumentConfig setDelay(int delay) {
		this.delay = delay;
		return this;
	}

	public Integer getIndex() {
		return index;
	}

	public ArgumentConfig setIndex(Integer index) {
		this.index = index;
		return this;
	}

	public String getType() {
		return type;
	}

	public ArgumentConfig setType(String type) {
		this.type = type;
		return this;
	}

	public ArgumentConfig setCallback(Boolean callback) {
		this.callback = callback;
		return this;
	}

	public Boolean isCallback() {
		return callback;
	}

}