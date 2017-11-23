
# Android 音效处理



## Android音效处理实现 <br/>

android系统自从2.3以后就添加了音效处理的api,比如均衡器（Equalizer）、低音增强（BassBoot）、示波器（Visualizer）、环绕音（Virtualizer）、音效混响（PresetReverb）等功能。
直接通过系统的api很容易实现音效的基本处理功能，比如小米系统的音效处理功能：<br/>
<img src="https://github.com/wanliLiu/androidequalizer/blob/master/%E5%9D%87%E8%A1%A1%E5%99%A8/%E7%BD%91%E6%98%93%E4%BA%91%E5%B0%8F%E7%B1%B3/Screenshot_2017-11-21-11-43-15-621_com.android.mu.png" width="360" height="640"><br/>

所以，如果我们要做，利用系统api做成小米的这个样子，难度不大。

## 目前市场上主流的播放器 音效处理

- 百度音乐 酷狗音乐<br/>
    可调范围：±12dp<br/>
    均衡器频段：31Hz  62Hz  125Hz  250Hz  500Hz  1KHz  2KHz  4KHz  8KHz  16KHz<br/>
- 虾米<br/>
    可调范围：±15dp<br/>
    均衡器频段：31Hz  62Hz  125Hz  250Hz  500Hz  1KHz  2KHz  4KHz  8KHz  16KHz
- QQ音乐<br/>
    qq音乐好像用第三方一个super song DTS
    可调范围：±12dp<br/>
    均衡器频段：27Hz  55Hz  109Hz  219Hz  438Hz  875Hz  2KHz  4KHz  7KHz  14KHz<br/>
    但是最新的qq音乐播放器  把均衡器功能去掉了
- 三星 自带均衡器<br/>
    可调范围：±10db<br/>
    均衡器频段：60Hz  150Hz  400Hz  1KHz  3KHz  8KHz  16KHz<br/>
- 网易云音乐<br/>
    android的网易云用的是Android系统的，也就是系统有就有，没有就没有，没有自己做。
- Android的api里读取的<br/>
    可调范围：±15db<br/>
    均衡器频段：60Hz  230Hz  910Hz  3.6KHz  14KHz<br/>
     
### 相比之下，Android，除了网易云没有单独写EQ外，其他主流的播放器都有自己写EQ,而且预设音场相对系统提供的音场，要多得多。而且均衡器频段更宽。

### 其实音效处理这块，是比较需要具有音乐相关的专业知识才能做好音效处理这块,而且要花时间，光懂技术还不行，得知道不同音场下的各个参数应该设置多少，当然我们可以用系统提供的默任音场，只是很少而已。

## 所以针对我们目前的情况，我提议
- 如果要做，目前情况只能用系统默认的参数来做，就跟小米系统的那个差不多，
<div class='row'>
    <img src="https://github.com/wanliLiu/androidequalizer/blob/master/%E5%9D%87%E8%A1%A1%E5%99%A8/%E7%BD%91%E6%98%93%E4%BA%91%E5%B0%8F%E7%B1%B3/Screenshot_2017-11-21-11-43-19-045_com.android.mu.png" width="360" height="640">
    <img src="https://github.com/wanliLiu/androidequalizer/blob/master/pic/2.png" width="360" height="640">
</div>
- 不做，用系统的，如果系统有就有，没有就没有，跟Android网易云播放器一样
- 不做均衡器<br/>
 目前来说我们没必要一下子把所有的东西都做的要怎样，这个可以根据后面情况来看是否弄这块，而且这块是需要懂音乐的人配合来做，不然就简单做一个。你看酷狗的均衡器，是花了很多心思做的。能做到下载别人的音效文件。
 

# 我个人建议： 目前先不做均衡器





