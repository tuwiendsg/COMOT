/*******************************************************************************
 * Copyright 2014 Technische Universitat Wien (TUW), Distributed Systems Group E184
 *
 * This work was partially supported by the European Commission in terms of the
 * CELAR FP7 project (FP7-ICT-2011-8 \#317790)
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 *******************************************************************************/
package at.ac.tuwien.dsg.comot.m.common.exception;

/**
 * Thrown to propagate HTTP error from a core service.
 * 
 * @author Juraj
 *
 */
public class EpsException extends ComotException {

	private static final long serialVersionUID = 7350286958469243223L;

	protected int code;
	protected String msg;
	protected String componentName;
	protected boolean clientError;

	public EpsException(int code, String msg, String componentName) {
		super("HTTP code=" + code + ", message='" + msg + "'");

		this.code = code;
		this.msg = msg;
		this.componentName = componentName;

		if (code / 100 == 4) {
			clientError = true;
		}
	}

	public EpsException(String message) {
		super(message);
	}

	public EpsException(String message, Exception cause) {
		super(message, cause);
	}

	public int getCode() {
		return code;
	}

	public String getMsg() {
		return msg;
	}

	public boolean isClientError() {
		return clientError;
	}

	public String getComponentName() {
		return componentName;
	}

}
