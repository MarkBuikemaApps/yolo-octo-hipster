/*
 * Copyright 2013 Chris Banes
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
 */

package com.markbuikema.juliana32.ui.pulltorefresh;

import android.util.Log;
import android.view.View;

import com.markbuikema.juliana32.ui.ViewPagerCompatibleStaggeredGridView;

/**
 * FIXME
 */
public class ScrollYDelegate extends PullToRefreshAttacher.ViewDelegate {

	@Override
	public boolean isReadyForPull(View view, float x, float y) {
		if (view instanceof ViewPagerCompatibleStaggeredGridView) {
			ViewPagerCompatibleStaggeredGridView sgv = ((ViewPagerCompatibleStaggeredGridView) view);
			boolean atTop = sgv.getChildCount() == 0 || (sgv.isAtTop() && sgv.getChildAt(0).getTop() >= 0);
			Log.d("isReadyForPull", "at top: " + atTop);
			return atTop;
		} else
			return view.getScrollY() <= 0;
	}
}
