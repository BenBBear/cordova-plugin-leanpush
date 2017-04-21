declare namespace LeanPush {
    /**
     * 初始化函数
     */
    function init(): any;

    function onNotificationReceived(callbak: (installation: any) => any): any;
    function getInstallation(callbak: (value: any) => any): any;
    function subscribe(channel: string, success?: (value?: any) => any, error?: (value?: any) => any);
    function unsubscribe(channel: string, success?: (value?: any) => any, error?: (value?: any) => any);
    function clearSubscription(success?: (value?: any) => any, error?: (value?: any) => any);
}

export = LeanPush;