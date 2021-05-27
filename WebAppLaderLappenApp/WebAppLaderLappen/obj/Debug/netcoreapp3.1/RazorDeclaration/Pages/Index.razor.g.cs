// <auto-generated/>
#pragma warning disable 1591
#pragma warning disable 0414
#pragma warning disable 0649
#pragma warning disable 0169

namespace WebAppLaderLappen.Pages
{
    #line hidden
    using System;
    using System.Collections.Generic;
    using System.Linq;
    using Microsoft.AspNetCore.Components;
#nullable restore
#line 1 "C:\Users\emilp\Desktop\Laderlappen\WebAppLaderLappenApp\WebAppLaderLappen\_Imports.razor"
using System.Net.Http;

#line default
#line hidden
#nullable disable
#nullable restore
#line 2 "C:\Users\emilp\Desktop\Laderlappen\WebAppLaderLappenApp\WebAppLaderLappen\_Imports.razor"
using Microsoft.AspNetCore.Authorization;

#line default
#line hidden
#nullable disable
#nullable restore
#line 3 "C:\Users\emilp\Desktop\Laderlappen\WebAppLaderLappenApp\WebAppLaderLappen\_Imports.razor"
using Microsoft.AspNetCore.Components.Authorization;

#line default
#line hidden
#nullable disable
#nullable restore
#line 4 "C:\Users\emilp\Desktop\Laderlappen\WebAppLaderLappenApp\WebAppLaderLappen\_Imports.razor"
using Microsoft.AspNetCore.Components.Forms;

#line default
#line hidden
#nullable disable
#nullable restore
#line 5 "C:\Users\emilp\Desktop\Laderlappen\WebAppLaderLappenApp\WebAppLaderLappen\_Imports.razor"
using Microsoft.AspNetCore.Components.Routing;

#line default
#line hidden
#nullable disable
#nullable restore
#line 6 "C:\Users\emilp\Desktop\Laderlappen\WebAppLaderLappenApp\WebAppLaderLappen\_Imports.razor"
using Microsoft.AspNetCore.Components.Web;

#line default
#line hidden
#nullable disable
#nullable restore
#line 7 "C:\Users\emilp\Desktop\Laderlappen\WebAppLaderLappenApp\WebAppLaderLappen\_Imports.razor"
using Microsoft.JSInterop;

#line default
#line hidden
#nullable disable
#nullable restore
#line 8 "C:\Users\emilp\Desktop\Laderlappen\WebAppLaderLappenApp\WebAppLaderLappen\_Imports.razor"
using WebAppLaderLappen;

#line default
#line hidden
#nullable disable
#nullable restore
#line 9 "C:\Users\emilp\Desktop\Laderlappen\WebAppLaderLappenApp\WebAppLaderLappen\_Imports.razor"
using WebAppLaderLappen.Shared;

#line default
#line hidden
#nullable disable
#nullable restore
#line 4 "C:\Users\emilp\Desktop\Laderlappen\WebAppLaderLappenApp\WebAppLaderLappen\Pages\Index.razor"
using WebAppLaderLappen.Data;

#line default
#line hidden
#nullable disable
#nullable restore
#line 5 "C:\Users\emilp\Desktop\Laderlappen\WebAppLaderLappenApp\WebAppLaderLappen\Pages\Index.razor"
using System.Threading.Tasks;

#line default
#line hidden
#nullable disable
    [Microsoft.AspNetCore.Components.RouteAttribute("/")]
    public partial class Index : Microsoft.AspNetCore.Components.ComponentBase
    {
        #pragma warning disable 1998
        protected override void BuildRenderTree(Microsoft.AspNetCore.Components.Rendering.RenderTreeBuilder __builder)
        {
        }
        #pragma warning restore 1998
#nullable restore
#line 7 "C:\Users\emilp\Desktop\Laderlappen\WebAppLaderLappenApp\WebAppLaderLappen\Pages\Index.razor"
       
    private PositionService positionService;
    private List<Position> positions = new List<Position>();
    private List<Position> oldPositions = new List<Position>();

    protected override async Task OnInitializedAsync()
    {
        positionService = new PositionService();

    }
    protected override async void OnAfterRender(bool firstRender)
    {
        if (firstRender)
        {
            await JS.InvokeAsync<string>("createCanvas");
        }
        else
        {

        }
        try
        {
            await GetPos();
        }
        catch
        {

        }
    }

    private async Task GetPos()
    {
        while (true)
        {
            positions = await positionService.GetPositionsAsync();
            if (oldPositions.Count == positions.Count)
            {


            }
            else
            {
                oldPositions = await positionService.GetPositionsAsync();
                for (int i = 0; i < positions.Count; i++)
                {

                    try
                    {
                        await JS.InvokeAsync<string>("addMowerPos", positions[i].x / 100, positions[i].y / 100, positions[i].collision, positions[i].onLine);

                    }
                    catch
                    {

                    }
                }

            }

            await Task.Delay(1000);

        }
    }

#line default
#line hidden
#nullable disable
        [global::Microsoft.AspNetCore.Components.InjectAttribute] private IJSRuntime JS { get; set; }
    }
}
#pragma warning restore 1591
