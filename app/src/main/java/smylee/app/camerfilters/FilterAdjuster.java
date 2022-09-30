package smylee.app.camerfilters;

import com.daasuu.gpuv.egl.filter.GlFilter;

public interface FilterAdjuster {
    void adjust(GlFilter filter, int percentage);
}
