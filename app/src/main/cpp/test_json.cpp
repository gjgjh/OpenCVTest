//
// Created by gjh on 2019-06-06.
//

#include <jni.h>
#include <string>
#include "json/json.h"

extern "C"
JNIEXPORT jstring JNICALL
Java_com_example_opencvtest_JniJsonActivity_outputJsonCode(JNIEnv *env, jclass this_, jstring name_,
                                                           jstring age_, jstring sex_,
                                                           jstring type_) {
    const char *name = env->GetStringUTFChars(name_, 0);
    const char *age = env->GetStringUTFChars(age_, 0);
    const char *sex = env->GetStringUTFChars(sex_, 0);
    const char *type = env->GetStringUTFChars(type_, 0);

    Json::Value root;
    root["name"] = name;
    root["age"] = age;
    root["sex"] = sex;
    root["type"] = type;

    env->ReleaseStringUTFChars(name_, name);
    env->ReleaseStringUTFChars(age_, age);
    env->ReleaseStringUTFChars(sex_, sex);
    env->ReleaseStringUTFChars(type_, type);

    return env->NewStringUTF(root.toStyledString().c_str());
}

extern "C"
JNIEXPORT jstring JNICALL
Java_com_example_opencvtest_JniJsonActivity_parseJsonCode(JNIEnv *env, jclass type,
                                                          jstring json_str_) {
    const char *json_str = env->GetStringUTFChars(json_str_, 0);

    std::string out_str;
    Json::CharReaderBuilder b;
    Json::CharReader *reader(b.newCharReader());
    Json::Value root;
    JSONCPP_STRING errs;
    bool ok = reader->parse(json_str, json_str + std::strlen(json_str), &root, &errs);
    if (ok && errs.size() == 0) {
        std::string name = root["name"].asString();
        std::string age = root["age"].asString();
        std::string sex = root["sex"].asString();
        std::string type = root["type"].asString();
        out_str = "name: " + name + "\nage: " + age + "\nsex:" + sex + "\ntype: " + type + "\n";
    }

    env->ReleaseStringUTFChars(json_str_, json_str);

    return env->NewStringUTF(out_str.c_str());
}