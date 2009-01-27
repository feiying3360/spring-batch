/*
 * Copyright 2006-2007 the original author or authors.
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
package org.springframework.batch.core.listener;

import java.util.Collections;
import java.util.List;

import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.support.PatternMatcher;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

import com.sun.org.apache.xerces.internal.impl.xpath.XPath.Step;

/**
 * This class can be used to automatically promote items from the {@link Step}
 * {@link ExecutionContext} to the {@link Job} {@link ExecutionContext} at the
 * end of a step. A list of keys should be provided that correspond to the items
 * in the {@link Step} {@link ExecutionContext} that should be promoted.
 * 
 * Additionally, an optional list of statuses can be set to indicate for which
 * exit status codes the promotion should occur. These statuses will be checked
 * using the {@link PatternMatcher}, so wildcards are allowed.
 * 
 * @author Dan Garrette
 * @since 2.0
 */
public class ExecutionContextPromotionListener extends StepExecutionListenerSupport implements InitializingBean {

	private List<String> keys = null;
	private List<String> statuses = Collections.singletonList(ExitStatus.COMPLETED.getExitCode());

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.batch.core.domain.StepListener#afterStep(StepExecution
	 *      stepExecution)
	 */
	public ExitStatus afterStep(StepExecution stepExecution) {
		if (statuses == null) {
			this.performPromotion(stepExecution);
		}
		else {
			String exitCode = stepExecution.getExitStatus().getExitCode();
			for (String statusPattern : statuses) {
				if (PatternMatcher.match(statusPattern, exitCode)) {
					this.performPromotion(stepExecution);
				}
			}
		}

		return null;
	}

	private void performPromotion(StepExecution stepExecution) {
		ExecutionContext stepContext = stepExecution.getExecutionContext();
		ExecutionContext jobContext = stepExecution.getJobExecution().getExecutionContext();
		for (String key : keys) {
			jobContext.put(key, stepContext.get(key));
		}
	}

	public void afterPropertiesSet() throws Exception {
		Assert.notNull(this.keys, "The 'keys' property must be provided");
		Assert.notEmpty(this.statuses, "The 'keys' property must not be empty");
		Assert.notNull(this.statuses, "The 'statuses' property must be provided");
		Assert.notEmpty(this.statuses, "The 'statuses' property must not be empty");
	}

	/**
	 * @param keys
	 *            A list of keys corresponding to items in the {@link Step}
	 *            {@link ExecutionContext} that must be promoted.
	 */
	public void setKeys(List<String> keys) {
		this.keys = keys;
	}

	/**
	 * @param statuses
	 *            A list of statuses for which the promotion should occur.
	 *            Statuses can may contain wildcards recognizable by the
	 *            {@link PatternMatcher} class.
	 */
	public void setStatuses(List<String> statuses) {
		this.statuses = statuses;
	}
}
