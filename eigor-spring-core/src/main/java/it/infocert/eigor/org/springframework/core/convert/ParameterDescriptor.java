/*
 * Copyright 2002-2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package it.infocert.eigor.org.springframework.core.convert;

import it.infocert.eigor.org.springframework.core.GenericCollectionTypeResolver;
import it.infocert.eigor.org.springframework.core.MethodParameter;

import java.lang.annotation.Annotation;

/**
 * @author Keith Donald
 * @since 3.1
 */
class ParameterDescriptor extends AbstractDescriptor {

	private final MethodParameter methodParameter;


	public ParameterDescriptor(MethodParameter methodParameter) {
		super(methodParameter.getParameterType());
		this.methodParameter = methodParameter;
	}

	private ParameterDescriptor(Class<?> type, MethodParameter methodParameter) {
		super(type);
		this.methodParameter = methodParameter;
	}


	@Override
	public Annotation[] getAnnotations() {
		if (this.methodParameter.getParameterIndex() == -1) {
			return TypeDescriptor.nullSafeAnnotations(this.methodParameter.getMethodAnnotations());
		}
		else {
			return TypeDescriptor.nullSafeAnnotations(this.methodParameter.getParameterAnnotations());
		}
	}

	@Override
	protected Class<?> resolveCollectionElementType() {
		return GenericCollectionTypeResolver.getCollectionParameterType(this.methodParameter);
	}

	@Override
	protected Class<?> resolveMapKeyType() {
		return GenericCollectionTypeResolver.getMapKeyParameterType(this.methodParameter);
	}

	@Override
	protected Class<?> resolveMapValueType() {
		return GenericCollectionTypeResolver.getMapValueParameterType(this.methodParameter);
	}

	@Override
	protected AbstractDescriptor nested(Class<?> type, int typeIndex) {
		MethodParameter methodParameter = new MethodParameter(this.methodParameter);
		methodParameter.increaseNestingLevel();
		methodParameter.setTypeIndexForCurrentLevel(typeIndex);
		return new ParameterDescriptor(type, methodParameter);
	}

}
