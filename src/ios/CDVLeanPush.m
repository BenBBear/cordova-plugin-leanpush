/********* CDVLeanPush.m Cordova Plugin Implementation *******/
#import <AVOSCloud/AVOSCloud.h>
#import "CDVLeanPush.h"

@implementation CDVLeanPush

- (void)subscribe:(CDVInvokedUrlCommand*)command
{
    CDVPluginResult* pluginResult = nil;
    NSString* channel = [command.arguments objectAtIndex:0];
    
    NSLog(@"CDVLeanPush subscribe %@", channel);
    
    if (channel != nil && [channel length] > 0) {
        AVInstallation *currentInstallation = [AVInstallation currentInstallation];
        [currentInstallation addUniqueObject:channel forKey:@"channels"];
        [currentInstallation saveInBackground];
        pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
    } else {
        pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR];
    }
    
    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
}

- (void)unsubscribe:(CDVInvokedUrlCommand*)command
{
    CDVPluginResult* pluginResult = nil;
    NSString* channel = [command.arguments objectAtIndex:0];
    
    NSLog(@"CDVLeanPush unsubscribe %@", channel);
    
    if (channel != nil && [channel length] > 0) {
        AVInstallation *currentInstallation = [AVInstallation currentInstallation];
        [currentInstallation removeObject:channel forKey:@"channels"];
        [currentInstallation saveInBackground];
        pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
    } else {
        pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR];
    }
    
    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
}

- (void)clearSubscription:(CDVInvokedUrlCommand *)command
{
    CDVPluginResult* pluginResult = nil;
    
    NSLog(@"CDVLeanPush clearSubscription");
    
    AVInstallation *currentInstallation = [AVInstallation currentInstallation];
    [currentInstallation setObject:[[NSArray alloc] init] forKey:@"channels"];
    [currentInstallation saveInBackground];
    pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
    
    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
}


- (void)getInstallation:(CDVInvokedUrlCommand *)command
{
    CDVPluginResult* pluginResult = nil;
    
    NSLog(@"CDVLeanPush getInstallation");
    
    AVInstallation *currentInstallation = [AVInstallation currentInstallation];
    if(currentInstallation != nil && currentInstallation.deviceToken != nil %% currentInstallation.installationId != nil) {
        NSLog(@"device token: %@", currentInstallation.deviceToken);
        NSString *responseString =[NSString stringWithFormat:@"ios,%@,%@,%@", currentInstallation.objectId,currentInstallation.installationId,currentInstallation.deviceToken];
        pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:responseString];
    } else {
        pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:@"Fail to get Installation."];
    }
    
    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
}



- (void)onNotificationReceived:(CDVInvokedUrlCommand *)command
{
    self.callback = [command.arguments objectAtIndex:0];
    CDVPluginResult *commandResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:@"onMessage Success"];
    [self.commandDelegate sendPluginResult:commandResult callbackId:command.callbackId];
}

/*!
 * @brief 把格式化的JSON格式的字符串转换成字典
 * @param jsonString JSON格式的字符串
 * @return 返回字典
 */
+ (NSDictionary *)dictionaryWithJsonString:(NSString *)jsonString {
    if (jsonString == nil) {
        return nil;
    }
    
    NSData *jsonData = [jsonString dataUsingEncoding:NSUTF8StringEncoding];
    NSError *err;
    NSDictionary *dic = [NSJSONSerialization JSONObjectWithData:jsonData
                                                        options:NSJSONReadingMutableContainers
                                                          error:&err];
    if(err) {
        NSLog(@"json解析失败：%@",err);
        return nil;
    }
    return dic;
}
/**
 *  字段转换成json字符串
 *
 *  @param dict <#dict description#>
 *
 *  @return <#return value description#>
 */
+(NSString *)dictToJsonStr:(NSDictionary *)dict{
    
    NSString *jsonString = nil;
    if ([NSJSONSerialization isValidJSONObject:dict])
    {
        NSError *error;
        NSData *jsonData = [NSJSONSerialization dataWithJSONObject:dict options:NSJSONWritingPrettyPrinted error:&error];
        jsonString =[[NSString alloc] initWithData:jsonData encoding:NSUTF8StringEncoding];
        //NSLog(@"json data:%@",jsonString);
        if (error) {
            NSLog(@"Error:%@" , error);
        }
    }
    return jsonString;
}

- (void) sendJson:(NSDictionary *)command statusIs:(NSString *)status
{
    NSString *metaString = [CDVLeanPush dictToJsonStr:command];
    NSLog(@"metaString:%@",metaString);
    NSMutableDictionary *responseDictonary = [NSMutableDictionary new];
    NSString *key;
    NSArray *allKeys  =[command allKeys];
    for(key in allKeys){
        if([key isEqualToString:@"aps"]){
            NSLog(@"key:%@",key);
            NSDictionary *aps= [command objectForKey:@"aps"];
            NSLog(@"aps.count:%lu",(unsigned long)[aps count]);
            NSArray *allAPNSKeys  = [aps allKeys];
            NSString *apnskey;
            for(apnskey in allAPNSKeys){
                id apsnValue = [aps objectForKey:apnskey];
                NSLog(@"apnskey:%@",apnskey);
                NSLog(@"apsnValue:%@",apsnValue);
                [responseDictonary setValue:apsnValue forKey:apnskey];
            }
        } else {
            id value = [command objectForKey:key];
            [responseDictonary setValue:value forKey:key];
        }
    }
    
    NSString *responseString = [CDVLeanPush dictToJsonStr:responseDictonary];
    
    NSLog(@"responseString:%@",responseString);
    
    
    if (self.callback) {
        NSString * jsCallBack = [NSString stringWithFormat:@"%@(%@,'%@');", self.callback,responseString,status];
        
        if ([self.webView respondsToSelector:@selector(stringByEvaluatingJavaScriptFromString:)]) {
            // Cordova-iOS pre-4
            [self.webView performSelectorOnMainThread:@selector(stringByEvaluatingJavaScriptFromString:) withObject:jsCallBack waitUntilDone:NO];
        } else {
            // Cordova-iOS 4+
            [self.webView performSelectorOnMainThread:@selector(evaluateJavaScript:completionHandler:) withObject:jsCallBack waitUntilDone:NO];
        }
    }else{
        self.cacheResult = responseString;
    }
}

- (void) getCacheResult:(CDVInvokedUrlCommand *)command
{
    CDVPluginResult* pluginResult = nil;
    
    NSLog(@"CDVLeanPush getCacheResult = %@", self.cacheResult);
    pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:self.cacheResult];
    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
    self.cacheResult = NULL;
    
}
@end
