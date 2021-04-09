# project.assets.json

**项目资源描述文件**

```json
"targets": {
  "LeadChina.Opportunity.Model/1.0.0": {
        "type": "project",
        "framework": ".NETStandard,Version=v2.1",
        "dependencies": {
          "LeadChina.Framework": "1.0.0"
        },
        "compile": {
          "bin/placeholder/LeadChina.Opportunity.Model.dll": {}
        },
        "runtime": {
          "bin/placeholder/LeadChina.Opportunity.Model.dll": {}
        }
      },
      "LeadChina.Opportunity.Repository/1.0.0": {
        "type": "project",
        "framework": ".NETStandard,Version=v2.1",
        "dependencies": {
          "LeadChina.Opportunity.Model": "1.0.0"
        },
        "compile": {
          "bin/placeholder/LeadChina.Opportunity.Repository.dll": {}
        },
        "runtime": {
          "bin/placeholder/LeadChina.Opportunity.Repository.dll": {}
        }
      }    
}
"libraries": {
    "LeadChina.Opportunity.Model/1.0.0": {
      "type": "project",
      "path": "../LeadChina.Opportunity.Model/LeadChina.Opportunity.Model.csproj",
      "msbuildProject": "../LeadChina.Opportunity.Model/LeadChina.Opportunity.Model.csproj"
    },
    "LeadChina.Opportunity.Repository/1.0.0": {
      "type": "project",
      "path": "../LeadChina.Opportunity.Repository/LeadChina.Opportunity.Repository.csproj",
      "msbuildProject": "../LeadChina.Opportunity.Repository/LeadChina.Opportunity.Repository.csproj"
    }
}
"projectFileDependencyGroups": {
    ".NETStandard,Version=v2.1": [
      "LeadChina.Opportunity.Model >= 1.0.0",
      "LeadChina.Opportunity.Repository >= 1.0.0"
    ]
}
"project": {
    "restore": {
        "frameworks": {
        "netstandard2.1": {
          "targetAlias": "netstandard2.1",
          "projectReferences": {
            "D:\\WorkingDir\\GitLabRepo\\lead.service\\Opportunity\\LeadChina.Opportunity.Model\\LeadChina.Opportunity.Model.csproj": {
              "projectPath": "D:\\WorkingDir\\GitLabRepo\\lead.service\\Opportunity\\LeadChina.Opportunity.Model\\LeadChina.Opportunity.Model.csproj"
            },
            "D:\\WorkingDir\\GitLabRepo\\lead.service\\Opportunity\\LeadChina.Opportunity.Repository\\LeadChina.Opportunity.Repository.csproj": {
              "projectPath": "D:\\WorkingDir\\GitLabRepo\\lead.service\\Opportunity\\LeadChina.Opportunity.Repository\\LeadChina.Opportunity.Repository.csproj"
            }
          }
        }
      }
    }
}
```

