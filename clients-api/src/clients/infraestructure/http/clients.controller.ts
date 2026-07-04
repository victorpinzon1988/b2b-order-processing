import { Controller, Get, Param } from "@nestjs/common";
import { ClientResponseDto } from "./client-response.dto";
import { GetClientService } from "../../application/services/get-client.service";

@Controller('clients')
export class ClientsController{
    constructor(private readonly getClientService: GetClientService){}

    @Get(':clientId')
    async getClient(
        @Param('clientId') clientId: string,
    ): Promise<ClientResponseDto>{
        return this.getClientService.execute(clientId)
    }
}