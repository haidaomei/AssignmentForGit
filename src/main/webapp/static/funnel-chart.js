/**
 * 企航CRM 销售漏斗图（基于 ECharts 5 内置漏斗系列）。
 *
 * <p>需求文档 1.1 节要求："仪表盘和独立漏斗页使用 ECharts 连续曲线漏斗"。
 * 本文件使用 ECharts 内置的 {@code type: "funnel"} 系列类型绘制销售漏斗，
 * gap: 0 使各阶段无缝连接形成连续形态，sort: "none" 保持阶段顺序不变。
 * 数据维度固定为"按商机数量"。</p>
 *
 * <p>使用方式：JSP 加载 ECharts CDN 和本文件后调用
 * {@code QihangFunnel.create(容器, 后台JSON)}。</p>
 *
 * <p>外部依赖：window.echarts（CDN 引入的 ECharts 5.4+ 全局变量）</p>
 *
 * @see CRM需求文档.md 5.2 节、6.11 节
 */
(function (window) {
    "use strict";

    /* 六个漏斗阶段的品牌配色 */
    var COLORS = ["#4f6fd5", "#67aee8", "#5fc4bc", "#f1bd59", "#ed7b67", "#6e5bd5"];

    /**
     * 在指定容器中创建销售漏斗图，按商机数量展示。
     *
     * @param {HTMLElement} el - 容器 DOM 元素
     * @param {Array} raw  - 后台 Gson 序列化的阶段数组 [{name,value,amount},...]
     * @returns {Object|null} {chart: echartsInstance} 或 null
     */
    function create(el, raw) {
        if (!el || !window.echarts) { return null; }

        var stages = Array.isArray(raw) ? raw : [];

        /* 无数据时显示文字提示 */
        function hint() {
            el.innerHTML = "";
            var d = document.createElement("div");
            d.className = "funnel-empty";
            d.innerHTML =
                '<i class="fas fa-filter" style="font-size:42px;color:#c8d4e3;margin-bottom:14px;display:block;"></i>' +
                '<div style="font-size:15px;color:#728097;">暂无商机数据</div>' +
                '<div style="font-size:13px;color:#b0bfd1;margin-top:6px;">请先创建商机，系统将自动汇总各阶段分布</div>';
            el.appendChild(d);
        }

        if (stages.length === 0) {
            hint();
            return { chart: null };
        }

        var chart = window.echarts.init(el);
        var timer = null;

        /* 构造 ECharts 漏斗图数据，固定使用 value（商机数量）字段 */
        var data = [];
        for (var i = 0; i < stages.length; i++) {
            var s = stages[i];
            /* 安全提取数值：兼容 null/undefined/非数字 */
            var v = Number(s.value);
            if (isNaN(v) || !isFinite(v)) { v = 0; }
            data.push({
                name: s.name,
                value: v,
                itemStyle: {
                    color: COLORS[i % COLORS.length],
                    borderColor: "#ffffff",
                    borderWidth: 2,
                    borderRadius: 6
                }
            });
        }

        chart.setOption({
            tooltip: {
                trigger: "item",
                formatter: function (p) {
                    return p.name + "<br>商机数量：" + p.value;
                }
            },
            series: [{
                name: "销售漏斗",
                type: "funnel",
                left: "10%",
                right: "10%",
                top: 20,
                bottom: 20,
                width: "80%",
                minSize: "20%",
                maxSize: "100%",
                /* sort: "none" 保持后台 SQL ORDER BY 的阶段顺序 */
                sort: "none",
                /* gap: 0 消除阶段间间隙，形成连续漏斗 */
                gap: 0,
                label: {
                    show: true,
                    position: "inside",
                    fontSize: 14,
                    fontWeight: 600,
                    color: "#ffffff",
                    formatter: function (p) {
                        return p.name + "  " + p.value + " 个";
                    }
                },
                labelLine: { show: false },
                data: data,
                emphasis: {
                    itemStyle: {
                        shadowBlur: 20,
                        shadowColor: "rgba(0,0,0,0.2)"
                    },
                    label: { fontSize: 16 }
                }
            }]
        }, true);

        /* 窗口缩放时自动调整尺寸（200ms 防抖） */
        window.addEventListener("resize", function () {
            if (timer) { clearTimeout(timer); }
            timer = setTimeout(function () {
                if (chart && !chart.isDisposed()) { chart.resize(); }
            }, 200);
        });

        return { chart: chart };
    }

    window.QihangFunnel = { create: create };
}(window));
