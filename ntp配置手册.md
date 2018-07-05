# 服务器时间同步（ntp配置手册）

在分布式系统中将一台服务器作为时间服务器，其他服务器和它保持同步（同步并不能做到绝对的同步，因为网络传输存在一定的时差，允许在某一个范围内的误差）。

## NTP服务器
NTP服务器【Network Time Protocol（NTP）】是用来使计算机时间同步化的一种协议，它可以使计算机对其服务器或时钟源（如石英钟，GPS等等)做同步化，它可以提供高精准度的时间校正（LAN上与标准间差小于1毫秒，WAN上几十毫秒），且可介由加密确认的方式来防止恶毒的协议攻击。时间按NTP服务器的等级传播。按照离外部UTC源的远近把所有服务器归入不同的Stratum（层）中。

## NTP Server配置

### 1. 安装NTP
    
    以Ubuntu为例
    
    $ sudo apt-get install ntp

### 2. 配置NTP Server

    安装成功以后在 /etc 下回生成一个ntp.conf 文件
    
    # /etc/ntp.conf, configuration for ntpd; see ntp.conf(5) for help
    # 时间差异文件
    driftfile /var/lib/ntp/ntp.drift
    
    # Enable this if you want statistics to be logged.
    # 分析统计
    #statsdir /var/log/ntpstats/
    
    statistics loopstats peerstats clockstats
    filegen loopstats file loopstats type day enable
    filegen peerstats file peerstats type day enable
    filegen clockstats file clockstats type day enable
    
    # Specify one or more NTP servers.
    
    # Use servers from the NTP Pool Project. Approved by Ubuntu Technical Board
    # on 2011-02-08 (LP: #104525). See http://www.pool.ntp.org/join.html for
    # more information.
    # ntp 服务
    pool 0.ubuntu.pool.ntp.org iburst
    pool 1.ubuntu.pool.ntp.org iburst
    pool 2.ubuntu.pool.ntp.org iburst
    pool 3.ubuntu.pool.ntp.org iburst
    
    # Use Ubuntu's ntp server as a fallback.
    pool ntp.ubuntu.com
    
    # Access control configuration; see /usr/share/doc/ntp-doc/html/accopt.html for
    # details.  The web page <http://support.ntp.org/bin/view/Support/AccessRestrictions>
    # might also be helpful.
    #
    # Note that "restrict" applies to both servers and clients, so a configuration
    # that might be intended to block requests from certain clients could also end
    # up blocking replies from your own upstream servers.
    
    # By default, exchange time with everybody, but don't allow configuration.
    # 不允许来自公网的ipv4 和 ipv6 客户端访问
    restrict -4 default kod notrap nomodify nopeer noquery limited
    restrict -6 default kod notrap nomodify nopeer noquery limited
    
    # Local users may interrogate the ntp server more closely.
    让NTP Server和其自身保持同步，如果在/etc/ntp.conf中定义的server都不可用时，将使用local时间作为ntp服务提供给ntp客户端.
    restrict 127.0.0.1
    restrict ::1
    
    # Needed for adding pool entries
    restrict source notrap nomodify noquery
    
    # Clients from this (example!) subnet have unlimited access, but only if
    # cryptographically authenticated.
    #restrict 192.168.125.0 mask 255.255.255.0 notrust
    # 允许192.168.125这个网段的同步请求.
    restrict 192.168.125.0 mask 255.255.255.0 nomodify
    
    
    # If you want to provide time to your local subnet, change the next line.
    # (Again, the address is an example only.)
    #broadcast 192.168.123.255
    
    # If you want to listen to time broadcasts on your local subnet, de-comment the
    # next lines.  Please do this only if you trust everybody on the network!
    #disable auth
    #broadcastclient
    
    #Changes recquired to use pps synchonisation as explained in documentation:
    #http://www.ntp.org/ntpfaq/NTP-s-config-adv.htm#AEN3918
    
    #server 127.127.8.1 mode 135 prefer    # Meinberg GPS167 with PPS
    #fudge 127.127.8.1 time1 0.0042        # relative to PPS for my hardware
    
    #server 127.127.22.1                   # ATOM(PPS)
    #fudge 127.127.22.1 flag3 1            # enable PPS API
    
    
    
    最重要的修改是：
    restrict 192.168.125.0 mask 255.255.255.0 nomodify
    
### 3. 重启NTP Server

    $ service ntp restart
    

## NTP Client 配置

### 1. 安装NTP 同上
### 2. 配置 NTP Client

    # 时间差异文件
    driftfile /var/lib/ntp/ntp.drift
    
    # Enable this if you want statistics to be logged.
    # 分析统计
    #statsdir /var/log/ntpstats/
    
    statistics loopstats peerstats clockstats
    filegen loopstats file loopstats type day enable
    filegen peerstats file peerstats type day enable
    filegen clockstats file clockstats type day enable
    
    # Specify one or more NTP servers.
    
    # Use servers from the NTP Pool Project. Approved by Ubuntu Technical Board
    # on 2011-02-08 (LP: #104525). See http://www.pool.ntp.org/join.html for
    # more information.
    # ntp 服务
    # pool 0.ubuntu.pool.ntp.org iburst
    # pool 1.ubuntu.pool.ntp.org iburst
    # pool 2.ubuntu.pool.ntp.org iburst
    # pool 3.ubuntu.pool.ntp.org iburst
    
    # Use Ubuntu's ntp server as a fallback.
    # pool ntp.ubuntu.com
    
    # Access control configuration; see /usr/share/doc/ntp-doc/html/accopt.html for
    # details.  The web page <http://support.ntp.org/bin/view/Support/AccessRestrictions>
    # might also be helpful.
    #
    # Note that "restrict" applies to both servers and clients, so a configuration
    # that might be intended to block requests from certain clients could also end
    # up blocking replies from your own upstream servers.
    
    # By default, exchange time with everybody, but don't allow configuration.
    # 不允许来自公网的ipv4 和 ipv6 客户端访问
    restrict -4 default kod notrap nomodify nopeer noquery limited
    restrict -6 default kod notrap nomodify nopeer noquery limited
    
    # Local users may interrogate the ntp server more closely.
    让NTP Server和其自身保持同步，如果在/etc/ntp.conf中定义的server都不可用时，将使用local时间作为ntp服务提供给ntp客户端.
    restrict 127.0.0.1
    restrict ::1
    
    # Needed for adding pool entries
    restrict source notrap nomodify noquery
    
    # Clients from this (example!) subnet have unlimited access, but only if
    # cryptographically authenticated.
    #restrict 192.168.125.0 mask 255.255.255.0 notrust
    
    
    # If you want to provide time to your local subnet, change the next line.
    # (Again, the address is an example only.)
    #broadcast 192.168.123.255
    
    # If you want to listen to time broadcasts on your local subnet, de-comment the
    # next lines.  Please do this only if you trust everybody on the network!
    #disable auth
    #broadcastclient
    
    #Changes recquired to use pps synchonisation as explained in documentation:
    #http://www.ntp.org/ntpfaq/NTP-s-config-adv.htm#AEN3918
    
    #server 127.127.8.1 mode 135 prefer    # Meinberg GPS167 with PPS
    #fudge 127.127.8.1 time1 0.0042        # relative to PPS for my hardware
    
    #server 127.127.22.1                   # ATOM(PPS)
    #fudge 127.127.22.1 flag3 1            # enable PPS API
    
    server 192.168.125.6
    fudge 192.168.125.6   stratum 5
    
    
    
    重要修改：
    注释掉上层服务，使用指定服务同步时间
    # pool 0.ubuntu.pool.ntp.org iburst
    # pool 1.ubuntu.pool.ntp.org iburst
    # pool 2.ubuntu.pool.ntp.org iburst
    # pool 3.ubuntu.pool.ntp.org iburst
    # pool ntp.ubuntu.com
    
    指定同步时间的服务器 
    server 192.168.125.6
    fudge 192.168.125.6   stratum 5  # 5分钟同步一次
    
### 3. 手动同步一次
ntp 在做自动同步的时候如果本地时间和时间服务器的差异太大，ntp 同步不成功需要手动做一次同步。

    $ ntpdate 192.168.125.6
    
    注意：
    第一次使用ntpdate 会提示找不到命令需要安装ntpdate
    $ sudo apt install ntpdate
    
    如果执行命令之后出现：
    21 Jun 10:29:12 ntpdate[20707]: the NTP socket is in use, exiting
    
    并不表示同步成功，需要关闭ntp 之后在尝试。
    $ service ntp stop
    
    出现：
    21 Jun 10:30:44 ntpdate[20915]: adjust time server 192.168.125.6 offset -0.012547 sec
    表示同步成功。
    
### 3. 开启NTP 自动同步

    $ service ntp start
    
    # 查看ntp 启动状态
    $ service ntp status
    

## 使用crontab 做时间同步

    $ crontab -e
    */5 * * * * /usr/sbin/ntpdate 192.168.125.6 >/dev/null 2>&1
    
    查看crontab 执行情况
    修改rsyslog 配置
    $ sudo vi /etc/rsyslog.d/50-default.conf
    将 cron 解开注释
    
    重启rsyslog
    $ sudo service rsyslog restart
    
    查看日志
    $ tail -f /var/log/cron.log
    
    注意如果 cron 还未执行日志还未生成会提示：
    tail: cannot open '/var/log/cron.log' for reading: No such file or directory
    tail: no files remaining
    
    如果日志已经生成会出现执行记录：
    Jun 21 10:45:01 arthur CRON[22231]: (root) CMD (/usr/sbin/ntpdate 192.168.125.6 >/dev/null 2>&1)
    Jun 21 10:50:01 arthur CRON[22664]: (root) CMD (/usr/sbin/ntpdate 192.168.125.6 >/dev/null 2>&1)
    

# 参考资料
1. https://blog.csdn.net/qishi_blog/article/details/52793206
2. http://blog.kissdata.com/2014/10/28/ubuntu-ntp.html