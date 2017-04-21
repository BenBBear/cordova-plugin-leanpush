var path = require('path');
var fs = require('fs');

// For xml parsing
var xml2js = require('xml2js');
var parser = new xml2js.Parser();
var builder = new xml2js.Builder();

// Use Thunk instead of Callback
var thunks = require('thunks');
var thunk = thunks({
    onerror: function(error) {
        console.error('Error happens during installing cordova-plugin-leanpush, in the lpush-installer.js \n');
        console.error(error.message);
        console.error(error.stack);
    }
});

// Thunkify Function
var readFile = thunk.thunkify(fs.readFile),
    writeFile = thunk.thunkify(fs.writeFile),
    parseString = thunk.thunkify(parser.parseString),
    as = thunk.thunkify(function(x, cb) {        
        cb(null, x);
    });






// get APPID & APPKEY & PACKAGE_NAME
function getVariables(root) {

    var android_config_xml = path.join(root, 'platforms/android/res/xml/config.xml');

    return readFile(android_config_xml)(function(err, data) {
        if (err)
            throw err;

        return parseString(data.toString());

    })(function(err, result) {
        if (err)
            throw err;

        var app_id, app_key, package_name;
        package_name = result.widget.$.id;
        result.widget.preference.forEach(function(item) {
            if (item.$.name == 'leancloud-appid') {
                app_id = item.$.value;
            }
            if (item.$.name == 'leancloud-appkey') {
                app_key = item.$.value;
            }
        });

        return as({
            app_id: app_id,
            app_key: app_key,
            package_name: package_name
        });
    });
}


// use Variables to patch source
function patchSource(vars) {
    var root = vars.root;

    var lean_application_path = path.join(root, "platforms/android/src/leancloud/cordova/leanpush/LeanApplication.java"),
        android_manifest_path = path.join(root, "platforms/android/AndroidManifest.xml"),
        lpush_main_activity_path = path.join(root, 'plugins/cordova-plugin-leancloud/other/MainActivity.java');

    var app_id = vars.app_id,
        app_key = vars.app_key,
        package_name = vars.package_name;
    

    return readFile(android_manifest_path)(function(err, data) {
        if (err)
            throw err;
        return parseString(data.toString());
    })(function(err, result) {
        if (err)
            throw err;

        result.manifest.application[0].$['android:name'] = 'leancloud.cordova.leanpush.LeanApplication';
        var xml = builder.buildObject(result);
        return writeFile(android_manifest_path, xml);
    })(function(err) {
        if (err)
            throw err;

        return readFile(lean_application_path);
    })(function(err, data) {
        if (err)
            throw err;

        var source = data
            .toString()
            .replace('<%LEAN_APP_ID%>', app_id)
            .replace('<%LEAN_APP_KEY%>', app_key)
            .replace('<%PACKAGE_NAME%>', package_name);
        return writeFile(lean_application_path, source);
    })(function(err) {
        if (err)
            throw err;
        return readFile(lpush_main_activity_path);
    })(function(err, data){
        if(err)
            throw err;

        var main_activity_dir_path = path.join(root, 'platforms/android/src/');
        
        package_name
            .split('.')
            .forEach(function(name) {
                main_activity_dir_path = path.join(main_activity_dir_path, name);
            });

        var source = data
                .toString()
                .replace('<%PACKAGE_NAME%>', package_name);
        
        return writeFile(path.join(main_activity_dir_path, 'MainActivity.java'), source);
    });
}




module.exports = function(root, done) {
    getVariables(root)(function(err, vars){        
        vars.root = root;
        return patchSource(vars);
    })(function(err){
        if(err)
            throw err;
        done();
    });    
};
